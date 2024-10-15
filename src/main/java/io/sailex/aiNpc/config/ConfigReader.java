package io.sailex.aiNpc.config;

import io.sailex.aiNpc.exception.InvalidPropertyValueException;
import io.sailex.aiNpc.exception.PropertyNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ConfigReader {

	private static final Logger LOGGER = LogManager.getLogger(ConfigReader.class);
	private Properties configProperties;
	private final String configPropertiesPath;

	public ConfigReader(String configPropertiesPath) {
		this.configPropertiesPath = configPropertiesPath;
		loadProperties();
	}

	private void loadProperties() {
		try (InputStream input = ConfigReader.class.getClassLoader().getResourceAsStream(configPropertiesPath)) {
			configProperties = new Properties();
			configProperties.load(input);
		} catch (NullPointerException | IOException e) {
			LOGGER.error("Error loading properties", e);
			throw new NullPointerException("Error loading properties");
		}
	}

	public String getProperty(String key) {
		if (key == null) {
			throw new IllegalArgumentException("Property key cannot be null");
		}
		String property = configProperties.getProperty(key);
		if (property == null) {
			LOGGER.error("Property not found for key: {}", key);
			throw new PropertyNotFoundException(String.format("Property not found for key: %s", key));
		}
		return property;
	}

	public int getIntProperty(String key) {
		String property = getProperty(key);
		try {
			return Integer.parseInt(property);
		} catch (NumberFormatException e) {
			LOGGER.error("Invalid integer value '{}' for property key: {}", property, key);
			throw new InvalidPropertyValueException(
					String.format("Property value %s is not a valid integer for key: %s", key, e.getMessage()));
		}
	}
}
