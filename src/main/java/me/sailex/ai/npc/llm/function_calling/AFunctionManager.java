package me.sailex.ai.npc.llm.function_calling;

import me.sailex.ai.npc.database.resources.ResourcesProvider;
import me.sailex.ai.npc.history.ConversationHistory;
import me.sailex.ai.npc.NPCController;
import me.sailex.ai.npc.llm.ILLMClient;
import me.sailex.ai.npc.llm.function_calling.constant.Function;
import me.sailex.ai.npc.model.database.LLMFunction;
import me.sailex.ai.npc.model.database.Resource;
import me.sailex.ai.npc.util.ResourceRecommender;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.ArrayList;
import java.util.List;

public class AFunctionManager<T> implements IFunctionManager<T> {

    protected List<LLMFunction> vectorizedFunctions;

    //functions that must always be included in the llm request
    protected static final List<String> NEEDED_FUNCTIONS = List.of(Function.Name.CHAT);
    protected final List<LLMFunction> neededFunctions;

    protected final ILLMClient llmClient;

    protected static NPCController controller;
    protected static ResourcesProvider resourcesProvider;
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
        this.neededFunctions = new ArrayList<>();
    }

    protected List<Resource> getRelevantResources(String prompt) {
        List<Resource> relevantFunctions = ResourceRecommender.getMostRelevantResources(
                llmClient, prompt, vectorizedFunctions, 2, 0.55
        );
        relevantFunctions.addAll(neededFunctions);
        return relevantFunctions;
    }

    protected void addVectorizedFunction(LLMFunction function) {
        if (NEEDED_FUNCTIONS.contains(function.getName())) {
            this.neededFunctions.add(function);
        } else {
            this.vectorizedFunctions.add(function);
        }
    }

    @Override
    public List<T> getRelevantFunctions(String prompt) {
        throw new UnsupportedOperationException("Must be implemented in child function manager");
    }

    @Override
    public void vectorizeFunctions(List<T> rawFunctions) {
        throw new UnsupportedOperationException("Must be implemented in child function manager");
    }

}
