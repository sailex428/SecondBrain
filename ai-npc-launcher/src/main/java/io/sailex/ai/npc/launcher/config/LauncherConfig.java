package io.sailex.ai.npc.launcher.config;

import io.sailex.ai.npc.launcher.constants.ConfigConstants;

public class LauncherConfig extends AConfig {

	public LauncherConfig() {
		super("launcher-config");
	}

	@Override
	protected void setDefaultProperties() {
		properties.setProperty(ConfigConstants.NPC_LLM_OLLAMA_URL, "http://localhost:11434");
		properties.setProperty(ConfigConstants.NPC_LLM_OLLAMA_MODEL, "gemma2");

		properties.setProperty(ConfigConstants.NPC_LLM_OPENAI_MODEL, "gpt-4o-mini");
		properties.setProperty(ConfigConstants.NPC_LLM_OPENAI_API_KEY, "");
		properties.setProperty(ConfigConstants.NPC_LLM_TYPE, "openai");
		properties.setProperty(ConfigConstants.NPC_IS_HEADLESS, "true");
		properties.setProperty(ConfigConstants.NPC_SERVER_PORT, "25565");
		properties.setProperty(ConfigConstants.NPC_LLM_OPENAI_BASE_URL, "https://api.openai.com");
	}
}
