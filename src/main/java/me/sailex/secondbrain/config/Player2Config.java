package me.sailex.secondbrain.config;

import io.wispforest.endec.Endec;
import io.wispforest.endec.StructEndec;
import io.wispforest.endec.impl.StructEndecBuilder;
import me.sailex.secondbrain.llm.LLMType;

public class Player2Config implements LLMConfig {

    public static final StructEndec<Player2Config> ENDEC = StructEndecBuilder.of(
            Endec.STRING.fieldOf("voiceId", Player2Config::getVoiceId),
            Endec.STRING.fieldOf("skinUrl", Player2Config::getSkinUrl),
            Endec.BOOLEAN.fieldOf("isTTS", Player2Config::isTTS),
            Player2Config::new
    );

    private String voiceId;
    private String skinUrl;
    private boolean isTTS;

    public Player2Config() {
        this("", "", false);
    }
    public Player2Config(String voiceId, String skinUrl, boolean isTTS) {
        this.voiceId = voiceId;
        this.skinUrl = skinUrl;
        this.isTTS = isTTS;
    }

    @Override
    public LLMType getType() {
        return LLMType.PLAYER2;
    }

    public String getVoiceId() {
        return voiceId;
    }

    public void setVoiceId(String voiceId) {
        this.voiceId = voiceId;
    }

    public String getSkinUrl() {
        return skinUrl;
    }

    public void setSkinUrl(String skinUrl) {
        this.skinUrl = skinUrl;
    }

    public boolean isTTS() {
        return isTTS;
    }

    public void setTTS(boolean TTS) {
        isTTS = TTS;
    }
}
