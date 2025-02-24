package me.sailex.ai.npc.llm.function_calling;

import me.sailex.ai.npc.database.resources.ResourcesProvider;
import me.sailex.ai.npc.history.ConversationHistory;
import me.sailex.ai.npc.NPCController;
import me.sailex.ai.npc.llm.ILLMClient;
import me.sailex.ai.npc.model.database.LLMFunction;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.ArrayList;
import java.util.List;

public class AFunctionManager<T> implements IFunctionManager<T> {

    protected List<LLMFunction> vectorizedFunctions;

    protected final ILLMClient llmClient;

    protected static ResourcesProvider resourcesProvider;
    protected static NPCController controller;
    protected static ServerPlayerEntity npcEntity;
    protected static ConversationHistory history;

    protected AFunctionManager(
        ResourcesProvider resourcesProvider,
        NPCController controller,
        ServerPlayerEntity npcEntity,
        ConversationHistory history,
        ILLMClient llmClient
    ) {
        AFunctionManager.resourcesProvider = resourcesProvider;
        AFunctionManager.history = history;
        AFunctionManager.controller = controller;
        AFunctionManager.npcEntity = npcEntity;
        this.llmClient = llmClient;
        this.vectorizedFunctions = new ArrayList<>();
    }

    @Override
    public List<T> getRelevantFunctions(String prompt) {
        return List.of();
    }

    @Override
    public void vectorizeFunctions(List<T> rawFunctions) {}

}
