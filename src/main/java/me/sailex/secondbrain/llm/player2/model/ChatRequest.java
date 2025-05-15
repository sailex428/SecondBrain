package me.sailex.secondbrain.llm.player2.model;

import io.github.sashirestela.openai.common.tool.Tool;
import io.github.sashirestela.openai.domain.chat.ChatMessage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@AllArgsConstructor
@Getter
public class ChatRequest {

    private final List<ChatMessage> messages;
    private final List<Tool> tools;

    public void addMessage(ChatMessage message) {
        this.messages.add(message);
    }
}
