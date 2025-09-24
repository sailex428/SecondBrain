package me.sailex.secondbrain.llm.player2.model;

import me.sailex.secondbrain.llm.roles.Player2ChatRole;

import java.util.List;

public record Player2ResponseMessage(
    Player2ChatRole role,
    String content,
    List<ToolCall> tool_calls
) {}
