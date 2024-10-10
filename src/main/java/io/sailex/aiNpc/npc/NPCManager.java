package io.sailex.aiNpc.npc;

import com.mojang.authlib.GameProfile;
import io.sailex.aiNpc.config.ConfigReader;
import io.sailex.aiNpc.model.NPC;
import io.sailex.aiNpc.util.GameProfileBuilder;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.Getter;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;

public class NPCManager {

	private final GameProfileBuilder profileBuilder;

	@Getter
	private final Map<UUID, NPCEntity> npcEntities;

	public NPCManager(ConfigReader configReader) {
		this.profileBuilder = new GameProfileBuilder(configReader);
		this.npcEntities = new HashMap<>();
	}

	public void buildNPC(NPC npc, MinecraftServer server) {
		String npcName = npc.getName();
		GameProfile npcProfile = profileBuilder.getGameProfile(npcName, server);

		ServerWorld worldIn = server.getWorld(npc.getNpcState().getDimension());

		NPCEntity npcEntity = new NPCEntity(npcName, server, worldIn, npcProfile, npc.getNpcState());
		npcEntity.spawnNPC();
		npcEntities.put(npcProfile.getId(), npcEntity);
	}
}
