package me.sailex.secondbrain.llm.player2.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Chat(List<Choice> choices) {

    public ResponseMessage firstMessage() {
        return choices.getFirst().message();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Choice(ResponseMessage message) {}
}
