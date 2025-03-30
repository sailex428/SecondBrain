package me.sailex.secondbrain.config;

import io.wispforest.endec.Endec;
import io.wispforest.endec.StructEndec;
import io.wispforest.endec.impl.StructEndecBuilder;
import me.sailex.secondbrain.constant.Instructions;
import me.sailex.secondbrain.llm.LLMType;

import java.util.UUID;

public class NPCConfig implements Configurable {

	private String npcName = "Steve";
	private UUID uuid;
	private String llmDefaultPrompt = Instructions.getLlmSystemPrompt(npcName, Instructions.DEFAULT_CHARACTER_TRAITS);
	private LLMType llmType = LLMType.OLLAMA;
	private String ollamaUrl = "http://localhost:11434";
	private String openaiApiKey = "API_KEY";

	public NPCConfig(String npcName, UUID uuid) {
		this.npcName = npcName;
		this.uuid = uuid;
	}

	public NPCConfig(
		String npcName,
		String uuid,
		String llmDefaultPrompt,
		LLMType llmType,
		String ollamaUrl,
		String openaiApiKey
	) {
		this.npcName = npcName;
		this.uuid = UUID.fromString(uuid);
		this.llmDefaultPrompt = llmDefaultPrompt;
		this.llmType = llmType;
		this.ollamaUrl = ollamaUrl;
		this.openaiApiKey = openaiApiKey;
	}

	public static class Builder {

		private final NPCConfig npcConfig;

		public Builder(String npcName) {
			this.npcConfig = new NPCConfig(npcName, null);
		}

		public Builder llmDefaultPrompt(String llmDefaultPrompt) {
			npcConfig.setLlmDefaultPrompt(llmDefaultPrompt);
			return this;
		}

		public Builder llmType(LLMType llmType) {
			npcConfig.setLlmType(llmType);
			return this;
		}

		public Builder ollamaUrl(String ollamaUrl) {
			npcConfig.setOllamaUrl(ollamaUrl);
			return this;
		}

		public Builder openaiApiKey(String openaiApiKey) {
			npcConfig.setOpenaiApiKey(openaiApiKey);
			return this;
		}

		public NPCConfig build() {
			return npcConfig;
		}

	}

	public static Builder builder(String npcName) {
		return new Builder(npcName);
	}

	public String getNpcName() {
		return npcName;
	}

	public String getLlmDefaultPrompt() {
		return llmDefaultPrompt;
	}

	public LLMType getLlmType() {
		return llmType;
	}

	public String getOllamaUrl() {
		return ollamaUrl;
	}

	public String getOpenaiApiKey() {
		return openaiApiKey;
	}

	public void setLlmDefaultPrompt(String llmDefaultPrompt) {
		this.llmDefaultPrompt = Instructions.getLlmSystemPrompt(npcName, llmDefaultPrompt);
	}

	public void setLlmType(LLMType llmType) {
		this.llmType = llmType;
	}

	public void setOllamaUrl(String ollamaUrl) {
		this.ollamaUrl = ollamaUrl;
	}

	public void setOpenaiApiKey(String openaiApiKey) {
		this.openaiApiKey = openaiApiKey;
	}

	public UUID getUuid() {
		return uuid;
	}

	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}

	@Override
	public String getConfigName() {
		return npcName.toLowerCase();
	}

	public static final StructEndec<NPCConfig> ENDEC = StructEndecBuilder.of(
			Endec.STRING.fieldOf("npcName", NPCConfig::getNpcName),
			Endec.STRING.fieldOf("uuid", config -> config.getUuid().toString()),
			Endec.STRING.fieldOf("llmDefaultPrompt", NPCConfig::getLlmDefaultPrompt),
			Endec.forEnum(LLMType.class).fieldOf("llmType", NPCConfig::getLlmType),
			Endec.STRING.fieldOf("ollamaUrl", NPCConfig::getOllamaUrl),
			Endec.STRING.fieldOf("openaiApiKey", NPCConfig::getOpenaiApiKey),
			NPCConfig::new
	);

	@Override
	public String toString() {
		return "NPCConfig{npcName=" + npcName +
				",uuid=" + uuid +
				",llmType=" + llmType +
				",ollamaUrl=" + ollamaUrl +
				",openaiApiKey=" + openaiApiKey +
				",llmDefaultPrompt=" + llmDefaultPrompt + "}";
	}
}
