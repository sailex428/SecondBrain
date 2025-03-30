package me.sailex.secondbrain.constant;

import java.util.Set;

/**
 * Keys of the properties of the configuration files
 */
public class ConfigConstants {

	private ConfigConstants() {}

	//general
	public static final String LLM_TIMEOUT = "npc.llm.timeout";
	public static final String CONTEXT_CHUNK_RADIUS = "npc.context.chunk.radius";

	public static final Set<String> ALLOWED_KEYS = Set.of(LLM_TIMEOUT, CONTEXT_CHUNK_RADIUS);
}
