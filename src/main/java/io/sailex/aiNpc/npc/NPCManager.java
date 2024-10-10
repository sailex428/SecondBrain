package io.sailex.aiNpc.npc;

import com.mojang.authlib.GameProfile;
import io.sailex.aiNpc.model.NPC;
import io.sailex.aiNpc.util.GameProfileBuilder;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;
import lombok.Getter;
import net.minecraft.server.MinecraftServer;
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
			return () -> Text.literal(String.format("NPC with name %s already exists!", npcName))
					.withColor(0xFF0000);
		}

		NPCEntity npcEntity = new NPCEntity(npcName, server, worldIn, npcProfile, npc.getNpcState());
		npcEntity.spawnNPC();
		npcEntities.put(npcProfile.getId(), npcEntity);
		return () -> Text.literal(String.format("NPC with name %s created!", npcName))
				.withColor(0x00FF00);
	}
}
