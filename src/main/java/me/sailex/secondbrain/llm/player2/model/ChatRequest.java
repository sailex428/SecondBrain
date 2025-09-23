package me.sailex.secondbrain.llm.player2.model;

import io.github.sashirestela.openai.common.tool.Tool;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@AllArgsConstructor
@Getter
public class ChatRequest {

    private List<Player2ChatMessage> messages;
    private List<Tool> tools;

    public void addMessage(Player2ChatMessage message) {
        messages.add(message);
    }

}
