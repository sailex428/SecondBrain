package io.sailex.ai.npc.client.config;

import java.util.Properties;

import io.sailex.ai.npc.client.constant.ConfigConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Class to read properties from the system
 */
public class Config {

	private Config() {}

	private static final Logger LOGGER = LogManager.getLogger(Config.class);
	private static final Properties properties = System.getProperties();

	/**
	 * Get the property value for the given key
	 *
	 * @param key the key
	 * @return the property value
	 */
	public static String getProperty(String key) {
		String value = properties.getProperty(key);
		if (!ConfigConstants.NPC_LLM_OPENAI_API_KEY.equals(key)) {
			LOGGER.info("Property: {} : {}", key, value);
		}
		return validateProperty(value);
	}

	private static String validateProperty(String property) {
		if (property == null) {
			LOGGER.error("Property is null");
			return null;
		}
		return property;
	}
}
