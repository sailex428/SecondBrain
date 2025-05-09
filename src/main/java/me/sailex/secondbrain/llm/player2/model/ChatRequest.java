package me.sailex.secondbrain.llm.player2.model;

import io.github.sashirestela.openai.common.function.FunctionDef;
import io.github.sashirestela.openai.domain.chat.ChatMessage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@AllArgsConstructor
@Getter
public class ChatRequest {

    private final List<ChatMessage> message;
    private final List<FunctionDef> tools;

    public void addMessage(ChatMessage message) {
        this.message.add(message);
    }
}
