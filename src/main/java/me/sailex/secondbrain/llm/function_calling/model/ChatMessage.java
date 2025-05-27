package me.sailex.secondbrain.llm.function_calling.model;

import me.sailex.secondbrain.llm.roles.ChatRole;

public record ChatMessage(ChatRole role, String content) {

    public static ChatMessage of(ChatRole role, String content) {
        return new ChatMessage(role, content);
    }
}
