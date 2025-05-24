package me.sailex.secondbrain.llm.player2.model;

import io.github.sashirestela.openai.common.tool.Tool;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import me.sailex.secondbrain.llm.function_calling.model.ChatMessage;

import java.util.List;

@Builder
@AllArgsConstructor
@Getter
public class ChatRequest {

    private List<ChatMessage> messages;
    private List<Tool> tools;

    public void addMessage(ChatMessage message) {
        messages.add(message);
    }

}
