package io.sailex.aiNpc.npc;

import com.mojang.authlib.GameProfile;
import io.sailex.aiNpc.constant.ConfigConstants;
import io.sailex.aiNpc.model.NPC;
import io.sailex.aiNpc.util.FeedbackLogger;
import io.sailex.aiNpc.util.GameProfileBuilder;
import io.sailex.aiNpc.util.config.ModConfig;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;
import lombok.Getter;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

public class NPCManager {

	private final GameProfileBuilder profileBuilder;

	@Getter
	private final Map<UUID, NPCEntity> npcEntities;

	public NPCManager() {
		this.profileBuilder = new GameProfileBuilder();
		this.npcEntities = new HashMap<>();
	}

	public Supplier<Text> buildNPC(NPC npc, MinecraftServer server) {
		String npcName = npc.getName();
		GameProfile npcProfile = profileBuilder.getGameProfile(npcName, server);

		ServerWorld worldIn = server.getWorld(npc.getNpcState().getDimension());

		if (npcEntities.size() >= ModConfig.getIntProperty(ConfigConstants.NPC_ENTITIES_MAX_COUNT)) {
			return FeedbackLogger.logError("Maximum number of NPCs reached!");
		}

		if (npcEntities.containsKey(npcProfile.getId())) {
			return FeedbackLogger.logError(String.format("NPC with name %s already exists!", npcName));
		}

		for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
			if (player.getUuid().equals(npcProfile.getId())) {
				return FeedbackLogger.logError("Player with that name already exists on the server!");
			}
		}

		NPCEntity npcEntity = new NPCEntity(npcName, server, worldIn, npcProfile, npc.getNpcState());
		npcEntity.connectNPC();
		npcEntities.put(npcProfile.getId(), npcEntity);
		return FeedbackLogger.logInfo(String.format("NPC with name %s created!", npcName));
	}

	public Supplier<Text> removeNPC(String name, MinecraftServer server) {
		UUID npcId = profileBuilder.getGameProfile(name, server).getId();
		NPCEntity npcEntity = npcEntities.get(npcId);

		if (npcEntity == null) {
			return FeedbackLogger.logError(String.format("Cannot find NPC with name %s", name));
		}

		npcEntity.removeNPC();
		npcEntities.remove(npcId);
		return FeedbackLogger.logInfo(String.format("NPC with name %s removed!", name));
	}
}
