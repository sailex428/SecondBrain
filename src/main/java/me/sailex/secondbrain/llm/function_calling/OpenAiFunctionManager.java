package me.sailex.secondbrain.llm.function_calling;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import io.github.sashirestela.openai.common.function.FunctionDef;
import io.github.sashirestela.openai.common.function.Functional;
import me.sailex.secondbrain.context.ContextProvider;
import me.sailex.secondbrain.database.resources.ResourcesProvider;
import me.sailex.secondbrain.llm.LLMClient;
import me.sailex.secondbrain.llm.function_calling.constant.Function;
import me.sailex.secondbrain.llm.function_calling.constant.Property;
import me.sailex.secondbrain.common.NPCController;
import me.sailex.secondbrain.model.function_calling.OpenAiFunction;
import me.sailex.secondbrain.util.PromptFormatter;
import net.minecraft.util.math.BlockPos;

import java.util.List;
import java.util.stream.Collectors;

public class OpenAiFunctionManager extends AFunctionManager<FunctionDef> {

    public OpenAiFunctionManager(
        ResourcesProvider resourcesProvider,
        NPCController controller,
        ContextProvider contextProvider,
        LLMClient llmClient
    ) {
        super(resourcesProvider, controller, contextProvider, llmClient);
    }

    protected List<FunctionDef> createFunctions() {
        return List.of(
                defineFunction(Function.Name.CHAT, Function.Description.CHAT, Chat.class),
                defineFunction(Function.Name.MOVE_TO_COORDINATES, Function.Description.MOVE_TO_COORDINATES, MoveToCoordinates.class),
                defineFunction(Function.Name.MOVE_TO_ENTITY, Function.Description.MOVE_TO_ENTITY, MoveToEntity.class),
                defineFunction(Function.Name.MOVE_AWAY, Function.Description.MOVE_AWAY, MoveAway.class),
                defineFunction(Function.Name.MINE_BLOCK, Function.Description.MINE_BLOCK, MineBlock.class),
                defineFunction(Function.Name.DROP_ITEM, Function.Description.DROP_ITEM, DropItem.class),
                defineFunction(Function.Name.ATTACK_ENTITY, Function.Description.ATTACK_ENTITY, AttackEntity.class),
                defineFunction(Function.Name.GET_ENTITIES, Function.Description.GET_ENTITIES, GetEntities.class),
                defineFunction(Function.Name.GET_BLOCKS, Function.Description.GET_BLOCKS, GetBlocks.class),
                defineFunction(Function.Name.GET_RECIPES, Function.Description.GET_RECIPES, GetRecipes.class),
                defineFunction(Function.Name.GET_CONVERSATIONS, Function.Description.GET_CONVERSATIONS, GetConversations.class),
                defineFunction(Function.Name.STOP, Function.Description.STOP, Stop.class)
        );
    }

    public <T extends Functional> FunctionDef defineFunction(String name, String description, Class<T> clazz) {
        return FunctionDef.builder()
                .name(name)
                .description(description)
                .functionalClass(clazz)
                .strict(true)
                .build();
    }

    @Override
    public void vectorizeFunctions(List<FunctionDef> rawFunctions) {
        rawFunctions.forEach(function -> {
            OpenAiFunction vectorizedFunction = new OpenAiFunction(
                    function.getName(),
                    function,
                    llmClient.generateEmbedding(List.of(function.getDescription()))
            );
            this.vectorizedFunctions.add(vectorizedFunction);
        });
    }

    @Override
    public List<FunctionDef> getRelevantFunctions(String prompt) {
        return getRelevantResources(prompt).stream()
                .map(OpenAiFunction.class::cast)
                .map(OpenAiFunction::getFunction)
                .collect(Collectors.toList());
    }

    private static class Chat implements Functional {

        @JsonPropertyDescription(Property.Description.MESSAGE)
        @JsonProperty(required = true)
        private String message;

        @Override
        public Object execute() {
            controller.addGoal("chat", () -> controller.chat(message));
            return "chatted message " + message;
        }
    }

    private static class MoveToCoordinates implements Functional {

        @JsonProperty(required = true)
        private int x;
        @JsonProperty(required = true)
        private int y;
        @JsonProperty(required = true)
        private int z;

        @Override
        public Object execute() {
            controller.addGoal(Function.Name.MOVE_TO_COORDINATES, () -> controller.moveToCoordinates(new BlockPos(x, y, z)));
            return "moving to " + x + ", " + y + ", " + z;
        }
    }

    private static class MoveToEntity implements Functional {

        @JsonPropertyDescription(Property.Description.ENTITY_NAME)
        @JsonProperty(required = true)
        private String entityName;

        @JsonPropertyDescription(Property.Description.IS_PLAYER)
        @JsonProperty(required = true)
        private boolean isPlayer;

        @Override
        public Object execute() {
            controller.addGoal(Function.Name.MOVE_TO_ENTITY, () -> controller.moveToEntity(entityName, isPlayer));
            return "moving to " + entityName;
        }
    }

    private static class MoveAway implements Functional {
        @Override
        public Object execute() {
            controller.addGoal(Function.Name.MOVE_AWAY, controller::moveAway);
            return "moving away";
        }
    }

    private static class MineBlock implements Functional {

        @JsonPropertyDescription(Property.Description.BLOCK_TYPE)
        @JsonProperty(required = true)
        private String blockType;

        @JsonPropertyDescription(Property.Description.NUMBER_OF_BLOCKS)
        @JsonProperty(required = true)
        private int numberOfBlocks;

        @Override
        public Object execute() {
            controller.addGoal(Function.Name.MINE_BLOCK, () -> controller.mineBlock(blockType, numberOfBlocks));
            return "mining block of name " + blockType;
        }
    }

    private static class DropItem implements Functional {

        @JsonPropertyDescription(Property.Description.ITEM_NAME)
        @JsonProperty(required = true)
        private String itemName;

        @JsonPropertyDescription(Property.Description.DROP_ALL)
        @JsonProperty(required = true)
        private boolean dropAll;

        @Override
        public Object execute() {
            controller.addGoal(Function.Name.DROP_ITEM, () -> controller.dropItem(itemName, dropAll));
            return "drops item/s of name " + itemName;
        }
    }

    private static class AttackEntity implements Functional {

        @JsonPropertyDescription(Property.Description.ENTITY_NAME)
        @JsonProperty(required = true)
        private String entityName;

        @JsonPropertyDescription(Property.Description.IS_PLAYER)
        @JsonProperty(required = true)
        private boolean isPlayer;

        @Override
        public Object execute() {
            controller.addGoal(Function.Name.ATTACK_ENTITY, () -> controller.attackEntity(entityName, isPlayer));
            return "tries to attack the entity " + entityName;
        }
    }

    private static class Stop implements Functional {
        @Override
        public Object execute() {
            controller.addGoal(Function.Name.STOP, controller::cancelActions, true);
            return "stop all actions";
        }
    }

    private static class GetEntities implements Functional {
        @Override
        public Object execute() {
            return PromptFormatter.formatEntities(contextProvider.getCachedContext().nearbyEntities(), 15);
        }
    }

    private static class GetBlocks implements Functional {
        @Override
        public Object execute() {
            return PromptFormatter.formatBlocks(contextProvider.getCachedContext().nearbyBlocks(), 15);
        }
    }

    private static class GetRecipes implements Functional {

        @JsonPropertyDescription(Property.Description.ITEM_NAME)
        @JsonProperty(required = true)
        private String itemName;

        @Override
        public Object execute() {
            return PromptFormatter.formatRecipes(resourcesProvider.getRelevantRecipes(itemName));
        }
    }

    private static class GetConversations implements Functional {

        @JsonPropertyDescription(Property.Description.TOPIC)
        @JsonProperty(required = true)
        private String topic;

        @Override
        public Object execute() {
            return PromptFormatter.formatConversation(resourcesProvider.getRelevantConversations(topic));
        }
    }

}