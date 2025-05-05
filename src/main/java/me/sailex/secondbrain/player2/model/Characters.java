package me.sailex.secondbrain.player2.model;

import java.util.List;

public record Characters(List<Character> characters) {
    public record Character(
            String id,
            String name,
            String short_name,
            String description,
            List<String> voice_ids,
            Meta meta
    ) {
        public record Meta(String skin_url) {}
    }
}
