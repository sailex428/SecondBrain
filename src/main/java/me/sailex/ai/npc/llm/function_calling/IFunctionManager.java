package me.sailex.ai.npc.llm.function_calling;

import java.util.List;

public interface IFunctionManager<T> {

    List<T> getRelevantFunctions(String prompt);

}
