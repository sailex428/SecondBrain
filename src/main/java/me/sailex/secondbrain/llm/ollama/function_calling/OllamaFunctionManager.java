package me.sailex.secondbrain.llm.ollama.function_calling;

import io.github.ollama4j.tools.ToolFunction;
import io.github.ollama4j.tools.Tools;
import static io.github.ollama4j.tools.Tools.PromptFuncDefinition;

import me.sailex.altoclef.AltoClefController;
import me.sailex.secondbrain.llm.function_calling.AFunctionManager;
import me.sailex.secondbrain.llm.function_calling.util.ArgumentParser;
import me.sailex.secondbrain.llm.function_calling.constant.Function;
import me.sailex.secondbrain.llm.function_calling.constant.Property;
import me.sailex.secondbrain.util.PromptFormatter;
import net.minecraft.util.math.BlockPos;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class OllamaFunctionManager extends AFunctionManager<Tools.ToolSpecification> {

    public OllamaFunctionManager(AltoClefController controller) {
        super(controller);
    }

    protected List<Tools.ToolSpecification> createFunctions() {
        return List.of(
            defineFunction(Function.Name.MOVE_TO_COORDINATES, Function.Description.MOVE_TO_COORDINATES, NPCFunction::moveToCoordinates, new Tools.PropsBuilder()
                    .withProperty(Property.Name.X, Tools.PromptFuncDefinition.Property.builder().type("int").required(true).build())
                    .withProperty(Property.Name.Y, Tools.PromptFuncDefinition.Property.builder().type("int").required(true).build())
                    .withProperty(Property.Name.Z, Tools.PromptFuncDefinition.Property.builder().type("int").required(true).build())
                    .build(), List.of(Property.Name.X, Property.Name.Y, Property.Name.Z)),
            defineFunction(Function.Name.MOVE_TO_ENTITY, Function.Description.MOVE_TO_ENTITY, NPCFunction::moveToEntity, new Tools.PropsBuilder()
                    .withProperty(Property.Name.ENTITY_NAME, Tools.PromptFuncDefinition.Property.builder().type("string").description(Property.Description.PLAYER_NAME).required(true).build())
                    .withProperty(Property.Name.IS_PLAYER, Tools.PromptFuncDefinition.Property.builder().type("boolean").description(Property.Description.IS_PLAYER).required(true).build())
                    .build(), List.of(Property.Name.ENTITY_NAME, Property.Name.IS_PLAYER)),
            defineVoidFunction(Function.Name.MOVE_AWAY, Function.Description.MOVE_AWAY, NPCFunction::moveAway),
            defineFunction(Function.Name.MINE_BLOCK, Function.Description.MINE_BLOCK, NPCFunction::mineBlock, new Tools.PropsBuilder()
                    .withProperty(Property.Name.BLOCK_TYPE, Tools.PromptFuncDefinition.Property.builder().type("string").description(Property.Description.BLOCK_TYPE).required(true).build())
                    .withProperty(Property.Name.NUMBER_OF_BLOCKS, Tools.PromptFuncDefinition.Property.builder().type("int").description(Property.Description.NUMBER_OF_BLOCKS).required(true).build())
                    .build(), List.of(Property.Name.BLOCK_TYPE, Property.Name.NUMBER_OF_BLOCKS)),
            defineFunction(Function.Name.DROP_ITEM, Function.Description.DROP_ITEM, NPCFunction::dropItem, new Tools.PropsBuilder()
                    .withProperty(Property.Name.ITEM_NAME, Tools.PromptFuncDefinition.Property.builder().type("string").description(Property.Description.ITEM_NAME).required(true).build())
                    .withProperty(Property.Name.DROP_ALL, Tools.PromptFuncDefinition.Property.builder().type("boolean").description(Property.Description.DROP_ALL).required(false).build())
                    .build(), List.of(Property.Name.ITEM_NAME)),
            defineFunction(Function.Name.ATTACK_ENTITY, Function.Description.ATTACK_ENTITY, NPCFunction::attackEntity, new Tools.PropsBuilder()
                    .withProperty(Property.Name.ENTITY_NAME, Tools.PromptFuncDefinition.Property.builder().type("string").description(Property.Description.ENTITY_NAME).required(true).build())
                    .withProperty(Property.Name.IS_PLAYER, Tools.PromptFuncDefinition.Property.builder().type("boolean").description(Property.Description.IS_PLAYER).required(false).build())
                    .build(), List.of(Property.Name.ENTITY_NAME)),
            defineVoidFunction(Function.Name.GET_ENTITIES, Function.Description.GET_ENTITIES, NPCFunction::getEntities),
            defineVoidFunction(Function.Name.GET_BLOCKS, Function.Description.GET_BLOCKS, NPCFunction::getBlocks),
            defineFunction(Function.Name.GET_RECIPES, Function.Description.GET_RECIPES, NPCFunction::getRecipes, new Tools.PropsBuilder()
                    .withProperty(Property.Name.ITEM_NAME, Tools.PromptFuncDefinition.Property.builder().type("string").description(Property.Description.ITEM_NAME).required(true).build())
                    .build(), List.of(Property.Name.ITEM_NAME)),
            defineFunction(Function.Name.GET_CONVERSATIONS, Function.Description.GET_CONVERSATIONS, NPCFunction::getConversations, new Tools.PropsBuilder()
                    .withProperty(Property.Name.TOPIC, Tools.PromptFuncDefinition.Property.builder().type("string").description(Property.Description.TOPIC).required(true).build())
                    .build(), List.of(Property.Name.TOPIC)),
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

    private static class NPCFunction {

        public static String moveToCoordinates(Map<String, Object> arguments) {
            int x = ArgumentParser.getInt(arguments, Property.Name.X);
            int y = ArgumentParser.getInt(arguments, Property.Name.Y);
            int z = ArgumentParser.getInt(arguments, Property.Name.Z);

            controller.addGoal(Function.Name.MOVE_TO_COORDINATES, () -> controller.moveToCoordinates(new BlockPos(x, y, z)));
            return "moving to " + x + ", " + y + ", " + z;
        }

        public static String moveToEntity(Map<String, Object> arguments) {
            String entityName = ArgumentParser.getString(arguments,Property.Name.ENTITY_NAME);
            boolean isPlayer = ArgumentParser.getBoolean(arguments, Property.Name.IS_PLAYER);

            controller.addGoal(Function.Name.MOVE_TO_ENTITY, () -> controller.moveToEntity(entityName, isPlayer));
            return "moving to " + entityName;
        }

        public static String moveAway(Map<String, Object> arguments) {
            controller.addGoal(Function.Name.MOVE_AWAY, controller::moveAway);
            return "moving away";
        }

        public static String mineBlock(Map<String, Object> arguments) {
            String blockType = ArgumentParser.getString(arguments,Property.Name.BLOCK_TYPE);
            int numberOfBlocks = ArgumentParser.getInt(arguments, Property.Name.NUMBER_OF_BLOCKS);

            controller.addGoal(Function.Name.MINE_BLOCK, () -> controller.mineBlock(blockType, numberOfBlocks));
            return "mining block of name " + blockType;
        }

        public static String dropItem(Map<String, Object> arguments) {
            String itemName = ArgumentParser.getString(arguments,Property.Name.ITEM_NAME);
            boolean dropAll = ArgumentParser.getBoolean(arguments, Property.Name.DROP_ALL);

            controller.addGoal(Function.Name.DROP_ITEM, () -> controller.dropItem(itemName, dropAll));
            return "drops item/s of name " + itemName;
        }

        public static String attackEntity(Map<String, Object> arguments) {
            String entityName = ArgumentParser.getString(arguments,Property.Name.ENTITY_NAME);
            boolean isPlayer = ArgumentParser.getBoolean(arguments, Property.Name.IS_PLAYER);

            controller.addGoal(Function.Name.ATTACK_ENTITY, () -> controller.attackEntity(entityName, isPlayer));
            return "tries to attack the entity " + entityName;
        }

        public static String getEntities(Map<String, Object> arguments) {
            return PromptFormatter.formatEntities(contextProvider.getCachedContext().nearbyEntities(), 15);
        }

        public static String getBlocks(Map<String, Object> arguments) {
            return PromptFormatter.formatBlocks(contextProvider.getCachedContext().nearbyBlocks(), 15);
        }

        public static String getRecipes(Map<String, Object> arguments) {
            String itemName = ArgumentParser.getString(arguments, Property.Name.ITEM_NAME);

            return PromptFormatter.formatRecipes(resourcesProvider.getRelevantRecipes(itemName));
        }

        public static String getConversations(Map<String, Object> arguments) {
            String topic = ArgumentParser.getString(arguments, Property.Name.TOPIC);

            return PromptFormatter.formatConversation(resourcesProvider.getRelevantConversations(topic));
        }

        public static String stop(Map<String, Object> arguments) {
            controller.addGoal(Function.Name.STOP, controller::cancelActions, true);
            return "stop all actions";
        }
    }

}
