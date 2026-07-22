package me.sailex.secondbrain.config;

import io.wispforest.endec.Endec;
import io.wispforest.endec.StructEndec;
import me.sailex.secondbrain.llm.LLMType;

import java.util.Map;

public interface LLMConfig {

    LLMType getType();

    Map<LLMType, StructEndec<? extends LLMConfig>> ENDEC_MAP = Map.of(
            LLMType.OPENAI, OpenAiConfig.ENDEC,
            LLMType.OLLAMA, OllamaConfig.ENDEC,
            LLMType.PLAYER2, Player2Config.ENDEC
    );

    Endec<LLMConfig> ENDEC = Endec.dispatchedStruct(
            LLMConfig.ENDEC_MAP::get,
            LLMConfig::getType,
            Endec.forEnum(LLMType.class),
            "type"
    );
}
