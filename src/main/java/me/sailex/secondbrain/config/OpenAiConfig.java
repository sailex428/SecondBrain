package me.sailex.secondbrain.config;

import io.wispforest.endec.Endec;
import io.wispforest.endec.StructEndec;
import io.wispforest.endec.impl.StructEndecBuilder;
import me.sailex.secondbrain.llm.LLMType;

public class OpenAiConfig extends BaseLLMConfig {

    public static final StructEndec<OpenAiConfig> ENDEC = StructEndecBuilder.of(
            Endec.STRING.fieldOf("url", OpenAiConfig::getUrl),
            Endec.STRING.fieldOf("model", OpenAiConfig::getModel),
            Endec.STRING.fieldOf("apiKey", OpenAiConfig::getApiKey),
            OpenAiConfig::new
    );
    private static final String DEFAULT_URL = "https://api.openai.com/v1";
    private static final String DEFAULT_MODEL = "gpt-5.6-luna";

    private String apiKey;

    public OpenAiConfig() {
        this(DEFAULT_URL, DEFAULT_MODEL, "");
    }

    public OpenAiConfig(String url, String model, String apiKey) {
        super(url, model);
        this.apiKey = apiKey;
    }

    @Override
    public LLMType getType() {
        return LLMType.OPENAI;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
}
