package me.sailex.ai.npc.llm;

import java.util.List;

public interface IFunctionCaller<T> extends ILLMClient {

    /**
     * Executes functions that are called by openai / ollama based on the prompt
     *
     * @param source the source of the prompt e.g. system
     * @param prompt the prompt
     */
    String callFunctions(String source, String prompt, List<T> functions);

}
