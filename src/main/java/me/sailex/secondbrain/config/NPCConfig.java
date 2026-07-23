package me.sailex.secondbrain.config;

import me.sailex.secondbrain.constant.Instructions;
import java.util.UUID;

import io.wispforest.endec.Endec;
import io.wispforest.endec.StructEndec;
import io.wispforest.endec.impl.StructEndecBuilder;

public class NPCConfig implements Configurable {

	private String npcName = "Steve";
	private UUID uuid = UUID.randomUUID();
	private boolean isActive = true;
	private String llmCharacter = Instructions.DEFAULT_CHARACTER_TRAITS;
	private LLMConfig llm;

	public NPCConfig(String npcName) {
		this.npcName = npcName;
	}

	public NPCConfig(LLMConfig llm) {
		this.llm = llm;
	}

    public NPCConfig(
		String npcName,
		String uuid,
		boolean isActive,
		String llmCharacter,
		LLMConfig llm
	) {
		this.npcName = npcName;
		this.uuid = UUID.fromString(uuid);
		this.isActive = isActive;
		this.llmCharacter = llmCharacter;
		this.llm = llm;
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

		public Builder llmConfig(LLMConfig llmConfig) {
			npcConfig.setLlm(llmConfig);
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

	public void setLlmCharacter(String llmCharacter) {
		this.llmCharacter = llmCharacter;
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

	public LLMConfig getLlm() {
		return llm;
	}

	public void setLlm(LLMConfig llm) {
		this.llm = llm;
	}

	@Override
	public String getConfigName() {
		return npcName.toLowerCase();
	}

    public static final StructEndec<NPCConfig> ENDEC = StructEndecBuilder.of(
			Endec.STRING.fieldOf("npcName", NPCConfig::getNpcName),
			Endec.STRING.fieldOf("uuid", config -> config.getUuid().toString()),
			Endec.BOOLEAN.fieldOf("isActive", NPCConfig::isActive),
			Endec.STRING.fieldOf("llmCharacter", NPCConfig::getLlmCharacter),
			LLMConfig.ENDEC.fieldOf("llm", NPCConfig::getLlm),
			NPCConfig::new
	);

    public static NPCConfig deepCopy(NPCConfig config) {
        return new NPCConfig(
                config.npcName,
                config.uuid.toString(),
                config.isActive,
                config.llmCharacter,
                config.llm
        );
    }

	@Override
	public String toString() {
		return "NPCConfig{npcName=" + npcName +
				",uuid=" + uuid +
				",isActive=" + isActive +
				",llmCharacter=" + llmCharacter +
				",llmConfig=" + llm + "}";
	}

	//name for fields for npc config screen
	public static final String NPC_NAME = "Name of the NPC";
	public static final String EDIT_NPC = "Edit '%s'";
	public static final String LLM_CHARACTER = "Characteristics";
	public static final String LLM_TYPE = "Type";
    public static final String LLM_MODEL = "Model";
	public static final String URL = "URL";
	public static final String OPENAI_API_KEY = "OpenAI API Key";
	public static final String IS_TTS = "Text to Speech";
}
