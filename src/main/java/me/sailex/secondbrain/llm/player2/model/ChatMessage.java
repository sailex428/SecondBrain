package me.sailex.secondbrain.llm.player2.model;

import me.sailex.secondbrain.llm.roles.ChatRole;

public record ChatMessage(ChatRole role, String message) {

    public static ChatMessage of(ChatRole role, String message) {
        return new ChatMessage(role, message);
    }
}
