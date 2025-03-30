package me.sailex.secondbrain.llm.function_calling;

import java.util.List;

public interface FunctionManager<T> {

    List<T> getRelevantFunctions(String prompt);

    void vectorizeFunctions(List<T> functions);

}
