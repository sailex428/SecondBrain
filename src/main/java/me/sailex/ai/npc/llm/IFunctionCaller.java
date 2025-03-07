package me.sailex.ai.npc.llm;

import java.util.List;

public interface IFunctionCaller<T> extends ILLMClient {

    /**
     * Executes functions that are called by openai / ollama based on the prompt
     *
     * @param prompt the prompt
     */
    String callFunctions(String prompt, List<T> functions);

}
