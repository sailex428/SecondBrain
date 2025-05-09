package me.sailex.secondbrain.llm.player2.model;

import java.util.List;

public record TTSSpeakRequest(
   boolean play_in_app,
   int speed,
   String text,
   List<String> voice_ids
) {
    public TTSSpeakRequest(String text, List<String> voiceIds) {
        this(true, 1, text, voiceIds);
    }
}
