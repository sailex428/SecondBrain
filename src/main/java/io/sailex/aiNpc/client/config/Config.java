package io.sailex.aiNpc.client.config;

import java.util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Config {

	private static final Logger LOGGER = LogManager.getLogger(Config.class);
	private static final Properties properties = System.getProperties();

	public static String getProperty(String key) {
		return validateProperty(properties.getProperty(key));
	}

	private static String validateProperty(String property) {
		LOGGER.info("Property: {}", property);
		if (property == null) {
			LOGGER.error("Property is null");
			return null;
		}
		return property;
	}
}
