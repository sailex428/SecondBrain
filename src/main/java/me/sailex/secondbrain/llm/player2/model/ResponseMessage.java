package me.sailex.secondbrain.llm.player2.model;

import me.sailex.secondbrain.llm.roles.ChatRole;

import java.util.List;

public record ResponseMessage(
    ChatRole role,
    String content,
    List<ToolCall> tool_calls
) {}
