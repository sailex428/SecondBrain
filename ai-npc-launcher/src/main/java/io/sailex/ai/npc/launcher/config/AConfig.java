package io.sailex.ai.npc.launcher.config;

import static io.sailex.ai.npc.launcher.AiNPCLauncher.MOD_ID;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class AConfig {

	protected AConfig(String configName) {
		init(configName);
	}

	private static final Logger LOGGER = LogManager.getLogger(AConfig.class);
	protected Properties properties;
	private File configFile;

	protected abstract void setDefaultProperties();

	private void init(String configName) {
		properties = new Properties();

		File configDir = new File(FabricLoader.getInstance().getConfigDir().toFile(), MOD_ID);
		configDir.mkdirs();
		configFile = new File(configDir, configName + ".properties");
		setDefaultProperties();

		if (configFile.exists()) {
			loadProperties();
		} else {
			saveProperties();
		}
	}

	public boolean saveProperties() {
		try (FileOutputStream out = new FileOutputStream(configFile)) {
			properties.store(out, null);
			return true;
		} catch (IOException e) {
			LOGGER.error("Failed to save config file!", e);
			return false;
		}
	}

	private void loadProperties() {
		try (FileInputStream in = new FileInputStream(configFile)) {
			properties.load(in);
		} catch (IOException e) {
			LOGGER.error("Failed to load config file!", e);
			setDefaultProperties();
		}
	}

	public String getProperty(String key) {
		if (!properties.containsKey(key)) {
			LOGGER.error("Property key '{}' not found!", key);
			throw new IllegalArgumentException("Property key not found!");
		}
		return properties.getProperty(key);
	}

	public boolean setProperty(String key, String value) {
		properties.setProperty(key, value);
		return saveProperties();
	}
}
