package me.sailex.secondbrain.llm;

import me.sailex.secondbrain.exception.LLMServiceException;
import me.sailex.secondbrain.history.Message;

import java.util.List;

public interface FunctionCallable extends LLMClient {

    /**
     * Executes functions that are called by the llm based on the prompt
     *
     * @throws LLMServiceException if any errors occur while calling functions
     */
    Message callFunctions(List<Message> messages) throws LLMServiceException;

}
