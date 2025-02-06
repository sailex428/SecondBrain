package me.sailex.ai.npc.llm.function_calling;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import io.github.sashirestela.openai.common.function.FunctionDef;
import io.github.sashirestela.openai.common.function.FunctionExecutor;
import io.github.sashirestela.openai.common.function.Functional;
import lombok.Getter;
import lombok.Setter;
import me.sailex.ai.npc.context.ContextGenerator;
import me.sailex.ai.npc.database.resources.ResourcesProvider;
import me.sailex.ai.npc.history.ConversationHistory;
import me.sailex.ai.npc.model.context.WorldContext;
import me.sailex.ai.npc.npc.NPCController;
import me.sailex.ai.npc.npc.NPCInteraction;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.List;

@Setter
@Getter
public class OpenAiFunctionManager implements IFunctionManager {

    private FunctionExecutor functionExecutor;
    private static ResourcesProvider resourcesProvider;
    private static NPCController controller;
    private static ServerPlayerEntity npcEntity;
    private static ConversationHistory history;

    public OpenAiFunctionManager(
            NPCController controller,
            ResourcesProvider resourcesProvider,
            ServerPlayerEntity npcEntity,
            ConversationHistory history
    ) {
        OpenAiFunctionManager.controller = controller;
        OpenAiFunctionManager.resourcesProvider = resourcesProvider;
        OpenAiFunctionManager.npcEntity = npcEntity;
        OpenAiFunctionManager.history = history;
        functionExecutor = new FunctionExecutor();
        init();
    }

    private void init() {
        List<FunctionDef> functions = List.of(
                defineFunction("chat", "Print message into game chat.", Chat.class),
                defineFunction("move", "Move to the given location", Move.class),
                defineFunction("mine", "Mine the block at the given location", Mine.class),
                defineFunction("drop", "Drop one item of the given inventory slot", Drop.class),
                defineFunction("dropAll", "Drop all items of the given inventory slot", DropAll.class),
                defineFunction("attack", "Attack the entity of the given entity id", Attack.class),
                defineFunction("getEntities", "Get all entities and players next to the npc player (you)", GetEntities.class),
                defineFunction("getBlocks", "Get all blocks next to the player", GetBlocks.class),
                defineFunction("getNpcState", "Get npc state (foodlevel, health, ...) and inventory items", GetNpcState.class),
                defineFunction("getRecipes", "Get all recipes that matches the specified item", GetRecipes.class),
                defineFunction("getConversations", "Get a conversation to a specific topic from the past", GetConversations.class),
                defineFunction("getLatestConversations", "Get the last 7 conversations (user prompts and answer of you with the functions that are called)", GetLatestConversations.class),
                defineFunction("stop", "Stop all running npc actions (Should only be used if expressly requested)", Stop.class)
        );
        functions.forEach(functionExecutor::enrollFunction);
    }

    public <T extends Functional> FunctionDef defineFunction(String name, String description, Class<T> clazz) {
        return FunctionDef.builder()
                .name(name)
                .description(description)
                .functionalClass(clazz)
                .strict(true)
                .build();
    }

    private static class Chat implements Functional {

        @JsonPropertyDescription("Represents the message that will be printed in the game chat.")
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

        @JsonPropertyDescription("slot number of the item in the inventory")
        @JsonProperty(required = true)
        private int slot;

        @Override
        public Object execute() {
            controller.addAction(() -> controller.drop(slot), false);
            return "drops one item from slot " + slot;
        }
    }

    private static class DropAll implements Functional {

        @JsonPropertyDescription("slot number of the item in the inventory")
        @JsonProperty(required = true)
        private int slot;

        @Override
        public Object execute() {
            controller.addAction(() -> controller.dropAll(slot), false);
            return "drops all items from slot " + slot;
        }
    }

    private static class Attack implements Functional {

        @JsonPropertyDescription("entity id of the Entity that is to be attacked")
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
            return NPCInteraction.formatEntities(ContextGenerator.scanNearbyEntities(npcEntity));
        }
    }

    private static class GetBlocks implements Functional {
        @Override
        public Object execute() {
            return NPCInteraction.formatBlocks(ContextGenerator.scanNearbyBlocks(npcEntity));
        }
    }

    private static class GetNpcState implements Functional {
        @Override
        public Object execute() {
            return NPCInteraction.formatInventory(ContextGenerator.getInventoryState(npcEntity)) +
                    NPCInteraction.formatNPCState(ContextGenerator.getNpcState(npcEntity));
        }
    }

    private static class GetRecipes implements Functional {

        @JsonPropertyDescription("item for which a recipe is wanted")
        @JsonProperty(required = true)
        private String itemName;

        @Override
        public Object execute() {
            return NPCInteraction.formatRecipes(resourcesProvider.getRelevantRecipes(itemName));
        }
    }

    private static class GetConversations implements Functional {

        @JsonPropertyDescription("Topic for which old conversations should be searched for")
        @JsonProperty(required = true)
        private String topic;

        @Override
        public Object execute() {
            return NPCInteraction.formatConversation(resourcesProvider.getRelevantConversations(topic));
        }
    }

    private static class GetLatestConversations implements Functional {
        @Override
        public Object execute() {
            return history.getFormattedConversation();
        }
    }

}
