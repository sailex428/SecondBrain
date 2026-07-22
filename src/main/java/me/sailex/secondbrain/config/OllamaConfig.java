package me.sailex.secondbrain.config;

import io.wispforest.endec.Endec;
import io.wispforest.endec.StructEndec;
import io.wispforest.endec.impl.StructEndecBuilder;
import me.sailex.secondbrain.llm.LLMType;

public class OllamaConfig extends BaseLLMConfig {

    public static final StructEndec<OllamaConfig> ENDEC = StructEndecBuilder.of(
            Endec.STRING.fieldOf("url", OllamaConfig::getUrl),
            Endec.STRING.fieldOf("model", OllamaConfig::getModel),
            OllamaConfig::new
    );

    private static final String DEFAULT_URL = "http://localhost:11434";
    private static final String DEFAULT_MODEL = "qwen3:4b-instruct";

    public OllamaConfig() {
        this(DEFAULT_URL, DEFAULT_MODEL);
    }

    public OllamaConfig(String url, String model) {
        super(url, model);
    }

    @Override
    public LLMType getType() {
        return LLMType.OLLAMA;
    }
}
