package me.sailex.secondbrain.common;

import me.sailex.secondbrain.auth.UsernameValidator;
import me.sailex.secondbrain.config.ConfigProvider;
import me.sailex.secondbrain.config.NPCConfig;
import me.sailex.secondbrain.exception.LLMServiceException;
import me.sailex.secondbrain.llm.LLMType;
import me.sailex.secondbrain.llm.player2.Player2APIClient;
import me.sailex.secondbrain.llm.player2.model.Characters;
import me.sailex.secondbrain.util.LogUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Player2NpcSynchronizer {

    private static final int MAX_NPC_COUNT = 8;

    private final NPCService npcService;
    private final ConfigProvider configProvider;
    private final Player2APIClient player2APIClient;
    private final ScheduledExecutorService executor;

    public Player2NpcSynchronizer(NPCService npcService, ConfigProvider configProvider) {
        this.npcService = npcService;
        this.configProvider = configProvider;
        this.player2APIClient = new Player2APIClient();
        this.executor = Executors.newSingleThreadScheduledExecutor();
        scheduleHeartBeats();
    }

    /**
     * Synchronizes selected characters in player2 APP with ingame NPCs.
     * Retrieves selected characters from player2 API and spawns them (up to 8).
     * If a character is no longer included in selected characters, the NPC is also removed.
     *
     * @param spawnPos BlockPos where the NPCs will be spawned
     */
    public void syncCharacters(BlockPos spawnPos, MinecraftServer server, PlayerEntity owner) {
        try {
            this.player2APIClient.getHealthStatus();
            List<Characters.Character> characters = player2APIClient.getSelectedCharacters().characters();
            if (characters.size() > MAX_NPC_COUNT) {
                LogUtil.errorInChat("You selected more than " + MAX_NPC_COUNT + " characters in Player2. Limiting to " + MAX_NPC_COUNT);
            }
            Map<UUID, Characters.Character> uuidToChar = characters.stream()
                    .limit(MAX_NPC_COUNT)
                    .collect(Collectors.toMap(ch -> UUID.fromString(ch.id()),ch -> ch));
            List<UUID> currentNpcUuids = getNpcUuidsPlayer2();

            for (UUID uuid : currentNpcUuids) {
                npcService.deleteNpc(uuid, server.getPlayerManager());
            }
            configProvider.deleteByType(LLMType.PLAYER2);

            for (Map.Entry<UUID, Characters.Character> entry : uuidToChar.entrySet()) {
                Characters.Character character = entry.getValue();
                String normalizedName = UsernameValidator.normalizeUsername(character.short_name());
                NPCConfig config = NPCConfig.builder(normalizedName)
                        .uuid(entry.getKey())
                        .llmDefaultPrompt(character.description())
                        .llmType(LLMType.PLAYER2)
                        .voiceId(character.voice_ids().get(0))
                        .skinUrl(character.meta().skin_url())
                        .build();
                npcService.createNpc(config, server, spawnPos, owner);
            }
        } catch (Exception e) {
            LogUtil.errorInChat(e.getMessage());
            LogUtil.error(e);
        }
    }

    public void syncCharacters(MinecraftServer server, PlayerEntity owner) {
        this.syncCharacters(null, server, owner);
    }

    /**
     * Periodically sends health checks to the player2 API.
     */
    private void scheduleHeartBeats() {
        executor.scheduleAtFixedRate(() -> {
            try {
                List<UUID> uuids = getNpcUuidsPlayer2();
                if (uuids.isEmpty()) {
                    return;
                }
                this.player2APIClient.getHealthStatus();
            } catch (LLMServiceException e) {
                LogUtil.errorInChat(e.getMessage());
                LogUtil.error(e);
            }
        }, 0,1, TimeUnit.MINUTES);
    }

    public void shutdown() {
        executor.shutdownNow();
    }

    /**
     * Gets the NPC uuids that uses Player2 as LLM.
     */
    private List<UUID> getNpcUuidsPlayer2() {
        return npcService.getUuidToNpc()
                .entrySet().stream()
                .filter(e -> e.getValue().getConfig().getLlmType() == LLMType.PLAYER2)
                .map(Map.Entry::getKey)
                .toList();
    }

}
