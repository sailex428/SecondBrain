package me.sailex.secondbrain.llm.player2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.sashirestela.openai.common.tool.Tool;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;

@Builder
@AllArgsConstructor
public class Player2ChatRequest {

    @JsonProperty("messages")
    private List<Player2ChatMessage> messages;
    @JsonProperty("tools")
    private List<Tool> tools;

    public void addMessage(Player2ChatMessage message) {
        messages.add(message);
    }

}
