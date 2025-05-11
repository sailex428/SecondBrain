package me.sailex.secondbrain.config;

import io.wispforest.endec.Endec;
import io.wispforest.endec.StructEndec;
import io.wispforest.endec.impl.StructEndecBuilder;
import me.sailex.secondbrain.constant.Instructions;
import me.sailex.secondbrain.llm.LLMType;

import java.util.UUID;

public class NPCConfig implements Configurable {

	private String npcName = "Steve";
	private UUID uuid = UUID.randomUUID();
	private boolean isActive = true;
	private String llmCharacter = Instructions.DEFAULT_CHARACTER_TRAITS;
	private LLMType llmType = LLMType.OLLAMA;
	private String ollamaUrl = "http://localhost:11434";
	private String openaiApiKey = "API_KEY";

	public NPCConfig() {}

	public NPCConfig(String npcName) {
		this.npcName = npcName;
	}

	public NPCConfig(
		String npcName,
		String uuid,
		boolean isActive,
		String llmCharacter,
		LLMType llmType,
		String ollamaUrl,
		String openaiApiKey
	) {
		this.npcName = npcName;
		this.uuid = UUID.fromString(uuid);
		this.isActive = isActive;
		this.llmCharacter = llmCharacter;
		this.llmType = llmType;
		this.ollamaUrl = ollamaUrl;
		this.openaiApiKey = openaiApiKey;
	}

	public static class Builder {

		private final NPCConfig npcConfig;

		public Builder(String npcName) {
			this.npcConfig = new NPCConfig(npcName);
		}

		public Builder uuid(UUID uuid) {
			npcConfig.setUuid(uuid);
			return this;
		}

		public Builder llmDefaultPrompt(String llmDefaultPrompt) {
			npcConfig.setLlmCharacter(llmDefaultPrompt);
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

	public boolean isActive() {
		return isActive;
	}

	public String getLlmCharacter() {
		return llmCharacter;
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

	public void setLlmCharacter(String llmCharacter) {
		this.llmCharacter = llmCharacter;
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

	public void setActive(boolean active) {
		isActive = active;
	}

	public void setNpcName(String npcName) {
		this.npcName = npcName;
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
			Endec.BOOLEAN.fieldOf("isActive", NPCConfig::isActive),
			Endec.STRING.fieldOf("llmDefaultPrompt", NPCConfig::getLlmCharacter),
			Endec.forEnum(LLMType.class).fieldOf("llmType", NPCConfig::getLlmType),
			Endec.STRING.fieldOf("ollamaUrl", NPCConfig::getOllamaUrl),
			Endec.STRING.fieldOf("openaiApiKey", NPCConfig::getOpenaiApiKey),
			NPCConfig::new
	);

	@Override
	public String toString() {
		return "NPCConfig{npcName=" + npcName +
				",uuid=" + uuid +
				",isActive=" + isActive +
				",llmType=" + llmType +
				",ollamaUrl=" + ollamaUrl +
				",openaiApiKey=***" +
				",llmCharacter=" + llmCharacter + "}";
	}

	//name for fields for npc config screen
	public static final String NPC_NAME = "Name of the NPC";
	public static final String EDIT_NPC = "Edit '%s'";
	public static final String LLM_CHARACTER = "Characteristics";
	public static final String LLM_TYPE = "Type of the LLM";
	public static final String OLLAMA_URL = "Ollama URL";
	public static final String OPENAI_API_KEY = "OpenAI API Key";
}
