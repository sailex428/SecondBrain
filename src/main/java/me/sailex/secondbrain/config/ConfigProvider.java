package me.sailex.secondbrain.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import io.wispforest.endec.Endec;
import io.wispforest.endec.format.gson.GsonDeserializer;
import io.wispforest.endec.format.gson.GsonSerializer;
import me.sailex.secondbrain.llm.LLMType;
import me.sailex.secondbrain.util.LogUtil;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

import static me.sailex.secondbrain.SecondBrain.MOD_ID;

/**
 * Responsible for managing the configuration for NPCs and base settings.
 * It reads, updates and deletes configuration files in JSON format in the mods config directory.
*/
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

            this.npcConfigs = readAll(NPC_CONFIG_DIR, NPCConfig.ENDEC);
            this.baseConfig = readAll(BASE_CONFIG_DIR, BaseConfig.ENDEC).stream()
                    .findFirst()
                    .orElseGet(BaseConfig::new);
        } catch (IOException e) {
            LogUtil.error("Failed to load config: " + e.getMessage());
        }
    }

    private <T> List<T> readAll(Path dir, Endec<T> endec) throws IOException {
        try (Stream<Path> filenameStream = Files.list(dir)) {
            List<T> configs = new ArrayList<>();
            for (Path file : filenameStream.toList()) {
                configs.add(read(file, endec));
            }
            return configs;
        }
    }

    private <T> T read(Path file, Endec<T> endec) throws IOException {
        try (Reader reader = Files.newBufferedReader(file)) {
            JsonElement json = JsonParser.parseReader(reader);
            return endec.decodeFully(GsonDeserializer::of, json);
        }
    }

    private synchronized <T extends Configurable> void save(Path dir, T config, Endec<T> endec) {
        Path configPath = dir.resolve(config.getConfigName() + JSON_EXTENSION);
        try (Writer writer = Files.newBufferedWriter(configPath)) {
            JsonElement json = endec.encodeFully(GsonSerializer::of, config);
            GSON.toJson(json, writer);
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
        configsToRemove.forEach(config -> {
            npcConfigs.remove(config);
            delete(config.getConfigName());
        });
    }

    public synchronized void deleteByType(LLMType llmType) {
        npcConfigs.removeIf(config -> {
            if (config != null && config.getLlm().getType() == llmType) {
                delete(config.getConfigName());
                return true;
            }
            return false;
        });
    }

    public synchronized NPCConfig addNpcConfig(NPCConfig npcConfig) {
        npcConfigs.add(npcConfig);
        save(NPC_CONFIG_DIR, npcConfig, NPCConfig.ENDEC);
        return npcConfig;
    }

    /**
     * Updates an existing NPCConfig and saves it to disk.
     * If the updatedConfig has an empty api key for OpenAiConfig, it retains the old api key.
     */
    public synchronized void updateNpcConfig(NPCConfig updatedConfig) {
        NPCConfig oldConfig = getNpcConfig(updatedConfig.getUuid());
        if (oldConfig == null) return;

        if (oldConfig.getLlm() instanceof OpenAiConfig oldOpenAiConfig &&
                updatedConfig.getLlm() instanceof OpenAiConfig openAiConfig) {
            if (openAiConfig.getApiKey().isEmpty()) {
                openAiConfig.setApiKey(oldOpenAiConfig.getApiKey());
            }
        }
        npcConfigs.set(npcConfigs.indexOf(oldConfig), updatedConfig);
        save(NPC_CONFIG_DIR, updatedConfig, NPCConfig.ENDEC);

    }

    public NPCConfig getNpcConfig(UUID uuid) {
        return npcConfigs.stream()
                .filter(config -> config.getUuid().equals(uuid))
                .findFirst()
                .orElse(null);
    }

    public NPCConfig getNpcConfigByName(String npcName) {
        return npcConfigs.stream()
                .filter(config -> config.getNpcName().equals(npcName))
                .findFirst()
                .orElse(null);
    }

    public void setBaseConfig(BaseConfig baseConfig) {
        this.baseConfig = baseConfig;
        save(BASE_CONFIG_DIR, baseConfig, BaseConfig.ENDEC);
    }

    public List<NPCConfig> getNpcConfigs() {
        return npcConfigs;
    }

    public BaseConfig getBaseConfig() {
        return baseConfig;
    }
}