package me.sailex.ai.npc.llm.function_calling;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import io.github.sashirestela.openai.common.function.FunctionDef;
import io.github.sashirestela.openai.common.function.Functional;
import me.sailex.ai.npc.context.ContextGenerator;
import me.sailex.ai.npc.database.resources.ResourcesProvider;
import me.sailex.ai.npc.history.ConversationHistory;
import me.sailex.ai.npc.llm.ILLMClient;
import me.sailex.ai.npc.llm.function_calling.constant.Function;
import me.sailex.ai.npc.llm.function_calling.constant.Property;
import me.sailex.ai.npc.model.context.WorldContext;
import me.sailex.ai.npc.NPCController;
import me.sailex.ai.npc.util.PromptFormatter;
import net.minecraft.server.network.ServerPlayerEntity;

public class OpenAiFunctionManager extends AFunctionManager<FunctionDef> {

    public OpenAiFunctionManager(
            ResourcesProvider resourcesProvider,
            NPCController controller,
            ServerPlayerEntity npcEntity,
            ConversationHistory history,
            ILLMClient llmClient
    ) {
        super(resourcesProvider, controller, npcEntity, history, llmClient);
        createTools();
    }

    private void createTools() {
        defineFunction(Function.Name.CHAT, Function.Description.CHAT, Chat.class);
        defineFunction(Function.Name.MOVE, Function.Description.MOVE, Move.class);
        defineFunction(Function.Name.MINE, Function.Description.MINE, Mine.class);
        defineFunction(Function.Name.DROP, Function.Description.DROP, Drop.class);
        defineFunction(Function.Name.DROP_ALL, Function.Description.DROP_ALL, DropAll.class);
        defineFunction(Function.Name.ATTACK, Function.Description.ATTACK, Attack.class);
        defineFunction(Function.Name.GET_ENTITIES, Function.Description.GET_ENTITIES, GetEntities.class);
        defineFunction(Function.Name.GET_BLOCKS, Function.Description.GET_BLOCKS, GetBlocks.class);
        defineFunction(Function.Name.GET_NPC_STATE, Function.Description.GET_NPC_STATE, GetNpcState.class);
        defineFunction(Function.Name.GET_RECIPES, Function.Description.GET_RECIPES, GetRecipes.class);
        defineFunction(Function.Name.GET_CONVERSATIONS, Function.Description.GET_CONVERSATIONS, GetConversations.class);
        defineFunction(Function.Name.GET_LATEST_CONVERSATIONS, Function.Description.GET_LATEST_CONVERSATIONS, GetLatestConversations.class);
        defineFunction(Function.Name.STOP, Function.Description.STOP, Stop.class);
    }

    public <T extends Functional> void defineFunction(String name, String description, Class<T> clazz) {
         this.rawFunctions.add(
                 FunctionDef.builder()
                    .name(name)
                    .description(description)
                    .functionalClass(clazz)
                    .strict(true)
                    .build()
         );
    }

    private static class Chat implements Functional {

        @JsonPropertyDescription(Property.Description.MESSAGE)
        @JsonProperty(required = true)
        private String message;

        @Override
        public Object execute() {
            controller.addAction(() -> controller.chat(message), false);
            return "chat message " + message;
        }
    }

    private static class Move implements Functional {

        @JsonProperty(required = true)
        private int x;
        @JsonProperty(required = true)
        private int y;
        @JsonProperty(required = true)
        private int z;

        @Override
        public Object execute() {
            controller.addAction(() -> controller.move(new WorldContext.Position(x, y, z)), false);
            return "moving to " + x + ", " + y + ", " + z;
        }
    }

    private static class Mine implements Functional {

        @JsonProperty(required = true)
        private int x;
        @JsonProperty(required = true)
        private int y;
        @JsonProperty(required = true)
        private int z;

        @Override
        public Object execute() {
            controller.addAction(() -> controller.mine(new WorldContext.Position(x, y, z)), false);
            return "mining block at " + x + ", " + y + ", " + z;
        }
    }

    private static class Drop implements Functional {

        @JsonPropertyDescription(Property.Description.SLOT)
        @JsonProperty(required = true)
        private int slot;

        @Override
        public Object execute() {
            controller.addAction(() -> controller.drop(slot), false);
            return "drops one item from slot " + slot;
        }
    }

    private static class DropAll implements Functional {

        @JsonPropertyDescription(Property.Description.SLOT)
        @JsonProperty(required = true)
        private int slot;

        @Override
        public Object execute() {
            controller.addAction(() -> controller.dropAll(slot), false);
            return "drops all items from slot " + slot;
        }
    }

    private static class Attack implements Functional {

        @JsonPropertyDescription(Property.Description.ENTITY_ID)
        @JsonProperty(required = true)
        private int entityId;

        @Override
        public Object execute() {
            controller.addAction(() -> controller.attack(entityId), false);
            return "tries to attack the entity " + entityId;
        }
    }

    private static class Stop implements Functional {
        @Override
        public Object execute() {
            controller.addAction(() -> controller.cancelActions(), true);
            return "stop all actions";
        }
    }

    private static class GetEntities implements Functional {
        @Override
        public Object execute() {
            return PromptFormatter.formatEntities(ContextGenerator.scanNearbyEntities(npcEntity));
        }
    }

    private static class GetBlocks implements Functional {
        @Override
        public Object execute() {
            return PromptFormatter.formatBlocks(ContextGenerator.scanNearbyBlocks(npcEntity));
        }
    }

    private static class GetNpcState implements Functional {
        @Override
        public Object execute() {
            return PromptFormatter.formatInventory(ContextGenerator.getInventoryState(npcEntity)) +
                    PromptFormatter.formatNPCState(ContextGenerator.getNpcState(npcEntity));
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

    private static class GetLatestConversations implements Functional {
        @Override
        public Object execute() {
            return history.getFormattedConversation();
        }
    }

}
