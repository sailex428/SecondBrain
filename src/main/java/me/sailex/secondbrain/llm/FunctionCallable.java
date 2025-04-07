package me.sailex.secondbrain.llm;

import me.sailex.secondbrain.model.function_calling.FunctionResponse;

import java.util.List;

public interface FunctionCallable<T> extends LLMClient {

    /**
     * Executes functions that are called by the llm based on the prompt
     *
     * @param prompt the prompt
     */
    FunctionResponse callFunctions(String prompt, List<T> functions);

}
