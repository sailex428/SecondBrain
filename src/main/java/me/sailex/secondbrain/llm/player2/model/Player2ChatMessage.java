package me.sailex.secondbrain.llm.player2.model;

import me.sailex.secondbrain.llm.roles.ChatRole;

public record Player2ChatMessage(ChatRole role, String content) {

    public static Player2ChatMessage of(ChatRole role, String content) {
        return new Player2ChatMessage(role, content);
    }
}
