package me.sailex.secondbrain.llm.player2.model;

import java.util.List;

public record Characters(List<Character> characters) {
    public record Character(
            String id,
            String name,
            String short_name,
            String greeting,
            String description,
            List<String> voice_ids,
            Meta meta
    ) {
        public record Meta(String skin_url, String type) {}
    }
}
