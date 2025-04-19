package me.sailex.secondbrain.model.function_calling;

public record FunctionResponse(String finalResponse, String toolCalls) {}
