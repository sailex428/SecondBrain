package me.sailex.ai.npc.llm.function_calling;

import lombok.Getter;
import me.sailex.ai.npc.database.resources.ResourcesProvider;
import me.sailex.ai.npc.history.ConversationHistory;
import me.sailex.ai.npc.npc.NPCController;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class AFunctionManager<T> implements IFunctionManager<T> {

    /**
     * Map of all registered functions.
     * (function name mapped to function)
     */
    protected Map<String, T> nameToFunction;

    protected static ResourcesProvider resourcesProvider;
    protected static NPCController controller;
    protected static ServerPlayerEntity npcEntity;
    protected static ConversationHistory history;

    protected AFunctionManager(
        ResourcesProvider resourcesProvider,
        NPCController controller,
        ServerPlayerEntity npcEntity,
        ConversationHistory history
    ) {
        AFunctionManager.resourcesProvider = resourcesProvider;
        AFunctionManager.history = history;
        AFunctionManager.controller = controller;
        AFunctionManager.npcEntity = npcEntity;
        this.nameToFunction = new HashMap<>();
    }

    @Override
    public List<T> getRelevantFunctions(String prompt) {
        return List.of();
    }

}
