package me.sailex.secondbrain.llm.function_calling;

import java.util.List;

public interface FunctionProvider<T> {

    List<T> getFunctions();

}
