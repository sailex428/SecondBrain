package me.sailex.secondbrain.llm.function_calling;

@FunctionalInterface
public interface ChatCallback {

    void chat(String content);

}