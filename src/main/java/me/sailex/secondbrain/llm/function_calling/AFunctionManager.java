package me.sailex.secondbrain.llm.function_calling;

import me.sailex.secondbrain.context.ContextProvider;
import me.sailex.secondbrain.database.resources.ResourcesProvider;
import me.sailex.secondbrain.common.NPCController;
import me.sailex.secondbrain.llm.LLMClient;
import me.sailex.secondbrain.model.function_calling.LLMFunction;
import me.sailex.secondbrain.model.database.Resource;
import me.sailex.secondbrain.util.ResourceRecommender;

import java.util.ArrayList;
import java.util.List;

public abstract class AFunctionManager<T> implements FunctionManager<T> {

    protected List<LLMFunction> vectorizedFunctions;

    protected final LLMClient llmClient;
    protected static NPCController controller;
    protected static ResourcesProvider resourcesProvider;
    protected static ContextProvider contextProvider;

    protected AFunctionManager(
        ResourcesProvider resourcesProvider,
        NPCController controller,
        ContextProvider contextProvider,
        LLMClient llmClient
    ) {
        AFunctionManager.resourcesProvider = resourcesProvider;
        AFunctionManager.controller = controller;
        AFunctionManager.contextProvider = contextProvider;
        this.llmClient = llmClient;
        this.vectorizedFunctions = new ArrayList<>();

        vectorizeFunctions(createFunctions());
    }

    protected abstract List<T> createFunctions();

    protected List<Resource> getRelevantResources(String prompt) {
        return ResourceRecommender.getRelevantResources(
                llmClient, prompt, vectorizedFunctions, 3
        );
    }

}
