package me.sailex.secondbrain.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Setter;
import me.sailex.secondbrain.llm.LLMType;
import me.sailex.secondbrain.util.LogUtil;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

import static me.sailex.secondbrain.SecondBrain.MOD_ID;

@Setter
public class ConfigProvider {

    private static final Path CONFIG_DIR = FabricLoader.getInstance().getConfigDir().resolve(MOD_ID);
    private static final Path NPC_CONFIG_DIR = CONFIG_DIR.resolve("npc");
    private static final Path BASE_CONFIG_DIR = CONFIG_DIR.resolve("base");

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String JSON_EXTENSION = ".json";

    private List<NPCConfig> npcConfigs = Collections.synchronizedList(new ArrayList<>());
    private BaseConfig baseConfig = new BaseConfig();

    public ConfigProvider() {
        try {
            Files.createDirectories(NPC_CONFIG_DIR);
            Files.createDirectories(BASE_CONFIG_DIR);

            this.npcConfigs = loadAll(NPC_CONFIG_DIR, NPCConfig.class);
            this.baseConfig = loadAll(BASE_CONFIG_DIR, BaseConfig.class).stream()
                    .findFirst()
                    .orElseGet(BaseConfig::new);
        } catch (IOException e) {
            LogUtil.error("Failed to load config: " + e.getMessage());
        }
    }

    private <T> List<T> loadAll(Path dir, Class<T> configClass) throws IOException {
        try (Stream<Path> filenameStream = Files.list(dir)) {
            List<T> configs = new ArrayList<>();
            for (Path file : filenameStream.toList()) {
                try (Reader reader = Files.newBufferedReader(file)) {
                    configs.add(GSON.fromJson(reader, configClass));
                }
            }
            return configs;
        }
    }

    public void saveAll() {
        save(BASE_CONFIG_DIR, baseConfig);
        npcConfigs.forEach(config -> save(NPC_CONFIG_DIR, config));
        LogUtil.info("Saved all configs");
    }

    private synchronized void save(Path dir, Configurable config) {
        Path configPath = dir.resolve(config.getConfigName() + JSON_EXTENSION);
        try (Writer writer = Files.newBufferedWriter(configPath)) {
            GSON.toJson(config, writer);
        } catch (IOException e) {
            LogUtil.error("Failed to save config for: " + config.getConfigName());
        }
    }

    private synchronized void delete(String configName) {
        Path configPath = NPC_CONFIG_DIR.resolve(configName + JSON_EXTENSION);
        try {
            Files.deleteIfExists(configPath);
        } catch (IOException e) {
            LogUtil.error("Failed to delete config for: " + configName);
        }
    }

    public synchronized void deleteNpcConfig(UUID uuid) {
        List<NPCConfig> configsToRemove = new ArrayList<>();
        npcConfigs.forEach(config -> {
            if (config.getUuid().equals(uuid)) {
                configsToRemove.add(config);
            }
        });
        npcConfigs.removeAll(configsToRemove);
        configsToRemove.forEach(config -> delete(config.getConfigName()));
    }

    public synchronized void deleteByType(LLMType llmType) {
        npcConfigs.removeIf(config -> config != null && config.getLlmType() == llmType);
    }

    public synchronized void addNpcConfig(NPCConfig npcConfig) {
        npcConfigs.add(npcConfig);
    }

    public synchronized void updateNpcConfig(NPCConfig updatedConfig) {
        npcConfigs.forEach(config -> {
            if (config.getNpcName().equals(updatedConfig.getNpcName())) {
                npcConfigs.set(npcConfigs.indexOf(config), updatedConfig);
            }
        });
    }

    public Optional<NPCConfig> getNpcConfig(UUID uuid) {
        return npcConfigs.stream()
                .filter(config -> config.getUuid().equals(uuid))
                .findFirst();
    }

    public Optional<NPCConfig> getNpcConfigByName(String npcName) {
        return npcConfigs.stream()
                .filter(config -> config.getNpcName().equals(npcName))
                .findFirst();
    }

    public List<NPCConfig> getNpcConfigs() {
        return npcConfigs;
    }

    public BaseConfig getBaseConfig() {
        return baseConfig;
    }
}