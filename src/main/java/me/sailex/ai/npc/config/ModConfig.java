package me.sailex.ai.npc.config;

import me.sailex.ai.npc.constant.ConfigConstants;

public class ModConfig extends AConfig {

	public ModConfig() {
		super("launcher-config");
	}

	@Override
	protected void setDefaultProperties() {
		properties.setProperty(ConfigConstants.NPC_LLM_OLLAMA_URL, "http://localhost:11434");

		properties.setProperty(ConfigConstants.NPC_LLM_OPENAI_API_KEY, "");
		properties.setProperty(ConfigConstants.NPC_LLM_OPENAI_BASE_URL, "https://api.openai.com");
	}
}
