package me.sailex.secondbrain.config;

import me.sailex.secondbrain.llm.LLMType;

public abstract class BaseLLMConfig implements LLMConfig {

    private String model;
    private String url;

    protected BaseLLMConfig(String url, String model) {
        this.url = url;
        this.model = model;
    }

    @Override
    public abstract LLMType getType();

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
