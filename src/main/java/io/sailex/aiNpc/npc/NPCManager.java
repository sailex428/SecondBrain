package io.sailex.aiNpc.npc;

import com.mojang.authlib.GameProfile;
import io.sailex.aiNpc.constant.DefaultConstants;
import io.sailex.aiNpc.model.NPC;
import io.sailex.aiNpc.util.GameProfileBuilder;
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

		if (npcEntities.containsKey(npcProfile.getId())) {
			return () -> Text.literal(String.format(
							"%s NPC with name %s already exists!", DefaultConstants.LOGGER_PREFIX, npcName))
					.withColor(0xCD3543);
		}

		for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
			if (player.getUuid().equals(npcProfile.getId())) {
				return () -> Text.literal(String.format(
								"%s Player with that name already exists on the server!",
								DefaultConstants.LOGGER_PREFIX))
						.withColor(0xCD3543);
			}
		}

		NPCEntity npcEntity = new NPCEntity(npcName, server, worldIn, npcProfile, npc.getNpcState());
		npcEntity.spawnNPC();
		npcEntities.put(npcProfile.getId(), npcEntity);
		return () -> Text.literal(
						String.format("%s NPC with name %s created!", DefaultConstants.LOGGER_PREFIX, npcName))
				.withColor(0x0079FF);
	}
}
