package me.sailex.secondbrain.llm.player2.model;

import me.sailex.secondbrain.llm.roles.Player2ChatRole;

public record Player2ChatMessage(Player2ChatRole role, String content) {

    public static Player2ChatMessage of(Player2ChatRole role, String message) {
        return new Player2ChatMessage(role, message);
    }
}
