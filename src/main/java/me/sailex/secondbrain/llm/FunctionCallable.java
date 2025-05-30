package me.sailex.secondbrain.llm;

import me.sailex.secondbrain.exception.LLMServiceException;
import me.sailex.secondbrain.llm.roles.BasicRole;

import java.util.List;

public interface FunctionCallable<T> extends LLMClient {

    /**
     * Executes functions that are called by the llm based on the prompt
     *
     * @throws LLMServiceException if any errors occur while calling functions
     */
    String callFunctions(
        BasicRole role,
        String prompt,
        List<T> functions
    ) throws LLMServiceException;

}
