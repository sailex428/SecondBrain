package me.sailex.secondbrain.llm.player2.model;

import io.github.sashirestela.openai.common.function.FunctionCall;

public record ToolCall(
    Integer index,
    String id,
    String type,
    FunctionCall function
) {}