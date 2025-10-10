package me.sailex.secondbrain.llm.player2.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Chat(List<Choice> choices) {

    public Player2ResponseMessage firstMessage() {
        return choices.get(0).message();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Choice(Player2ResponseMessage message) {}
}
