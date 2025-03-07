package me.sailex.ai.npc.llm.function_calling;

import io.github.ollama4j.tools.ToolFunction;
import io.github.ollama4j.tools.Tools;
import static io.github.ollama4j.tools.Tools.PromptFuncDefinition;

import me.sailex.ai.npc.context.ContextGenerator;
import me.sailex.ai.npc.database.resources.ResourcesProvider;
import me.sailex.ai.npc.history.ConversationHistory;
import me.sailex.ai.npc.llm.OllamaClient;
import me.sailex.ai.npc.llm.function_calling.constant.Function;
import me.sailex.ai.npc.llm.function_calling.constant.Property;
import me.sailex.ai.npc.model.context.WorldContext;
import me.sailex.ai.npc.NPCController;
import me.sailex.ai.npc.model.database.OllamaFunction;
import me.sailex.ai.npc.util.PromptFormatter;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class OllamaFunctionManager extends AFunctionManager<Tools.ToolSpecification> {

    public OllamaFunctionManager(
        ResourcesProvider resourcesProvider,
        NPCController controller,
        ServerPlayerEntity npcEntity,
        ConversationHistory history,
        OllamaClient llmClient
    ) {
        super(resourcesProvider, controller, npcEntity, history, llmClient);
        List<Tools.ToolSpecification> rawFunctions = createFunctions();
        llmClient.registerFunctions(rawFunctions);
        vectorizeFunctions(rawFunctions);
    }

    private List<Tools.ToolSpecification> createFunctions() {
        return List.of(
            defineFunction(Function.Name.CHAT, Function.Description.CHAT, NPCFunction::chat, new Tools.PropsBuilder()
                .withProperty(Property.Name.MESSAGE, Tools.PromptFuncDefinition.Property.builder().type("string").description(Property.Description.MESSAGE).required(true).build())
                .build(), List.of(Property.Name.MESSAGE)),
            defineFunction(Function.Name.MOVE, Function.Description.MOVE, NPCFunction::move, new Tools.PropsBuilder()
                .withProperty(Property.Name.X, Tools.PromptFuncDefinition.Property.builder().type("int").required(true).build())
                .withProperty(Property.Name.Y, Tools.PromptFuncDefinition.Property.builder().type("int").required(true).build())
                .withProperty(Property.Name.Z, Tools.PromptFuncDefinition.Property.builder().type("int").required(true).build())
                .build(), List.of(Property.Name.X, Property.Name.Y, Property.Name.Z)),
            defineFunction(Function.Name.MINE, Function.Description.MINE, NPCFunction::mine, new Tools.PropsBuilder()
                .withProperty(Property.Name.X, Tools.PromptFuncDefinition.Property.builder().type("int").required(true).build())
                .withProperty(Property.Name.Y, Tools.PromptFuncDefinition.Property.builder().type("int").required(true).build())
                .withProperty(Property.Name.Z, Tools.PromptFuncDefinition.Property.builder().type("int").required(true).build())
                .build(), List.of(Property.Name.X, Property.Name.Y, Property.Name.Z)),
            defineFunction(Function.Name.ATTACK, Function.Description.ATTACK, NPCFunction::attack, new Tools.PropsBuilder()
                .withProperty(Property.Name.ENTITY_ID, Tools.PromptFuncDefinition.Property.builder().type("int").description(Property.Description.ENTITY_ID).required(true).build())
                .build(), List.of(Property.Name.ENTITY_ID)),
            defineFunction(Function.Name.DROP, Function.Description.DROP, NPCFunction::drop, new Tools.PropsBuilder()
                .withProperty(Property.Name.SLOT, Tools.PromptFuncDefinition.Property.builder().type("int").description(Property.Description.SLOT).required(true).build())
                .build(), List.of(Property.Name.SLOT)),
            defineFunction(Function.Name.DROP_ALL, Function.Description.DROP_ALL, NPCFunction::dropAll, new Tools.PropsBuilder()
                .withProperty(Property.Name.SLOT, Tools.PromptFuncDefinition.Property.builder().type("int").description(Property.Description.SLOT).required(true).build())
                .build(), List.of(Property.Name.SLOT)),
            defineVoidFunction(Function.Name.GET_BLOCKS, Function.Description.GET_BLOCKS, NPCFunction::getBlocks),
            defineVoidFunction(Function.Name.GET_ENTITIES, Function.Description.GET_ENTITIES, NPCFunction::getEntities),
            defineVoidFunction(Function.Name.GET_NPC_STATE, Function.Description.GET_NPC_STATE, NPCFunction::getNpcState),
            defineVoidFunction(Function.Name.GET_RECIPES, Function.Description.GET_RECIPES, NPCFunction::getRecipes),
            defineFunction(Function.Name.GET_CONVERSATIONS, Function.Description.GET_CONVERSATIONS, NPCFunction::getConversations, new Tools.PropsBuilder()
                .withProperty(Property.Name.TOPIC, Tools.PromptFuncDefinition.Property.builder().type("string").description(Property.Description.TOPIC).required(true).build())
                .build(), List.of(Property.Name.TOPIC)),
            defineVoidFunction(Function.Name.GET_LATEST_CONVERSATIONS, Function.Description.GET_LATEST_CONVERSATIONS, NPCFunction::getLatestConversations),
            defineVoidFunction(Function.Name.STOP, Function.Description.STOP, NPCFunction::stop)
        );
    }

    private Tools.ToolSpecification defineVoidFunction(
        String functionName,
        String functionDescription,
        ToolFunction toolFunction
    ) {
        return defineFunction(functionName, functionDescription, toolFunction,
                new Tools.PropsBuilder().build(), Collections.emptyList());
    }

    private Tools.ToolSpecification defineFunction(
        String functionName,
        String functionDescription,
        ToolFunction toolFunction,
        Map<String, PromptFuncDefinition.Property> properties,
        List<String> requiredFields
    ) {
        PromptFuncDefinition.PromptFuncSpec funcDefinition = PromptFuncDefinition.PromptFuncSpec.builder()
                .name(functionName)
                .description(functionDescription)
                .parameters(PromptFuncDefinition.Parameters.builder()
                        .type("object")
                        .properties(properties)
                        .required(requiredFields)
                        .build()
                ).build();
        return Tools.ToolSpecification.builder()
                .functionName(functionName)
                .functionDescription(functionDescription)
                .toolFunction(toolFunction)
                .toolPrompt(Tools.PromptFuncDefinition.builder()
                        .type("function")
                        .function(funcDefinition)
                        .build()
                ).build();
    }

    @Override
    public void vectorizeFunctions(List<Tools.ToolSpecification> rawFunctions) {
        rawFunctions.forEach(function -> {
            OllamaFunction vectorizedFunction = new OllamaFunction(
                    function.getFunctionName(),
                    function,
                    llmClient.generateEmbedding(List.of(function.getFunctionDescription()))
            );
            addVectorizedFunction(vectorizedFunction);
        });
    }

    @Override
    public List<Tools.ToolSpecification> getRelevantFunctions(String prompt) {
        return getRelevantResources(prompt).stream()
                .map(OllamaFunction.class::cast)
                .map(OllamaFunction::getFunction)
                .collect(Collectors.toList());
    }

    private static class NPCFunction {

        public static String chat(Map<String, Object> arguments) {
            String message = (String) arguments.get(Property.Name.MESSAGE);

            controller.addAction(() -> controller.chat(message), false);
            return "chatted message " + message;
        }

        public static String move(Map<String, Object> arguments) {
            int x = Integer.parseInt((String) arguments.get(Property.Name.X));
            int y = Integer.parseInt((String)arguments.get(Property.Name.Y));
            int z = Integer.parseInt((String)arguments.get(Property.Name.Z));

            controller.addAction(() -> controller.move(new WorldContext.Position(x, y, z)), false);
            return "moving to " + x + ", " + y + ", " + z;
        }

        public static String mine(Map<String, Object> arguments) {
            int x = Integer.parseInt((String)arguments.get(Property.Name.X));
            int y = Integer.parseInt((String)arguments.get(Property.Name.Y));
            int z = Integer.parseInt((String)arguments.get(Property.Name.Z));

            controller.addAction(() -> controller.mine(new WorldContext.Position(x, y, z)), false);
            return "mining block at " + x + ", " + y + ", " + z;
        }

        public static String drop(Map<String, Object> arguments) {
            int slot = Integer.parseInt((String) arguments.get(Property.Name.SLOT));

            controller.addAction(() -> controller.drop(slot), false);
            return "drops one item from slot " + slot;
        }

        public static String dropAll(Map<String, Object> arguments) {
            int slot = Integer.parseInt((String) arguments.get(Property.Name.SLOT));

            controller.addAction(() -> controller.dropAll(slot), false);
            return "drops all items from slot " + slot;
        }

        public static String attack(Map<String, Object> arguments) {
            int entityId = Integer.parseInt((String) arguments.get(Property.Name.ENTITY_ID));

            controller.addAction(() -> controller.attack(entityId), false);
            return "tries to attack the entity " + entityId;
        }

        public static String getEntities(Map<String, Object> arguments) {
            return PromptFormatter.formatEntities(ContextGenerator.scanNearbyEntities(npcEntity));
        }

        public static String getBlocks(Map<String, Object> arguments) {
            return PromptFormatter.formatBlocks(ContextGenerator.scanNearbyBlocks(npcEntity));
        }

        public static String getNpcState(Map<String, Object> arguments) {
            return PromptFormatter.formatInventory(ContextGenerator.getInventoryState(npcEntity)) +
                    PromptFormatter.formatNPCState(ContextGenerator.getNpcState(npcEntity));
        }

        public static String getRecipes(Map<String, Object> arguments) {
            String itemName = (String) arguments.get(Property.Name.ITEM_NAME);

            return PromptFormatter.formatRecipes(resourcesProvider.getRelevantRecipes(itemName));
        }

        public static String getConversations(Map<String, Object> arguments) {
            String topic = (String) arguments.get(Property.Name.TOPIC);

            return PromptFormatter.formatConversation(resourcesProvider.getRelevantConversations(topic));
        }

        public static String getLatestConversations(Map<String, Object> arguments) {
            return history.getFormattedConversation();
        }

        public static String stop(Map<String, Object> arguments) {
            controller.addAction(() -> controller.cancelActions(), true);
            return "stop all actions";
        }
    }

}
