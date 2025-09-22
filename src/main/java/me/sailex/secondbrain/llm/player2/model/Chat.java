package me.sailex.secondbrain.llm.player2.model;

import java.util.List;

public record Chat(List<Choice> choices) {

    public ResponseMessage firstMessage() {
        return choices.getFirst().message();
    }

    public record Choice(int index, ResponseMessage message) {}
}
