package me.sailex.secondbrain.config;

import me.sailex.secondbrain.constant.Instructions;
import me.sailex.secondbrain.llm.LLMType;
import java.util.UUID;

import io.wispforest.endec.Endec;
import io.wispforest.endec.StructEndec;
import io.wispforest.endec.impl.StructEndecBuilder;

public class NPCConfig implements Configurable {

	private String npcName = "Steve";
	private UUID uuid = UUID.randomUUID();
	private boolean isActive = true;
	private String llmCharacter = Instructions.DEFAULT_CHARACTER_TRAITS;
	private LLMType llmType = LLMType.OLLAMA;
	private String ollamaUrl = "http://localhost:11434";
    private String llmModel = "llama3.2";
	private String openaiApiKey = "";
	private String voiceId = "not set";
	private String skinUrl = "";

	private boolean isTTS = false;

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
        String llmModel,
		String ollamaUrl,
		String openaiApiKey,
		boolean isTTS,
		String voiceId,
		String skinUrl
	) {
		this.npcName = npcName;
		this.uuid = UUID.fromString(uuid);
		this.isActive = isActive;
		this.llmCharacter = llmCharacter;
		this.llmType = llmType;
        this.llmModel = llmModel;
		this.ollamaUrl = ollamaUrl;
		this.openaiApiKey = openaiApiKey;
		this.isTTS = isTTS;
		this.voiceId = voiceId;
		this.skinUrl = skinUrl;
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

		public Builder voiceId(String voiceId) {
			npcConfig.setVoiceId(voiceId);
			return this;
		}

		public Builder skinUrl(String skinUrl) {
			npcConfig.setSkinUrl(skinUrl);
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

    public String getLlmModel() {
        return llmModel;
    }

    public void setLlmModel(String llmModel) {
        this.llmModel = llmModel;
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

	public String getVoiceId() {
		return voiceId;
	}

	public void setVoiceId(String voiceId) {
		this.voiceId = voiceId;
	}

	public boolean isTTS() {
		return isTTS;
	}

	public void setTTS(boolean TTS) {
		isTTS = TTS;
	}

	public String getSkinUrl() {
		return skinUrl;
	}

	public void setSkinUrl(String skinUrl) {
		this.skinUrl = skinUrl;
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
            Endec.STRING.fieldOf("llmModel", NPCConfig::getLlmModel),
			Endec.STRING.fieldOf("ollamaUrl", NPCConfig::getOllamaUrl),
			Endec.STRING.fieldOf("openaiApiKey", NPCConfig::getOpenaiApiKey),
			Endec.BOOLEAN.fieldOf("isTTS", NPCConfig::isTTS),
			Endec.STRING.fieldOf("voiceId", NPCConfig::getVoiceId),
			Endec.STRING.fieldOf("skinUrl", NPCConfig::getSkinUrl),
			NPCConfig::new
	);

    public static NPCConfig deepCopy(NPCConfig config) {
        return new NPCConfig(
                config.npcName,
                config.uuid.toString(),
                config.isActive,
                config.llmCharacter,
                config.llmType,
                config.llmModel,
                config.ollamaUrl,
                config.openaiApiKey,
                config.isTTS,
                config.voiceId,
                config.skinUrl
        );
    }

	@Override
	public String toString() {
		return "NPCConfig{npcName=" + npcName +
				",uuid=" + uuid +
				",isActive=" + isActive +
				",llmType=" + llmType +
				",ollamaUrl=" + ollamaUrl +
				",openaiApiKey=***" +
				",llmCharacter=" + llmCharacter +
				",voiceId=" + voiceId + "}";
	}

	//name for fields for npc config screen
	public static final String NPC_NAME = "Name of the NPC";
	public static final String EDIT_NPC = "Edit '%s'";
	public static final String LLM_CHARACTER = "Characteristics";
	public static final String LLM_TYPE = "Type";
    public static final String LLM_MODEL = "LLM Model";
	public static final String OLLAMA_URL = "Ollama URL";
	public static final String OPENAI_API_KEY = "OpenAI API Key";
	public static final String IS_TTS = "Text to Speech";
}
