package io.sailex.aiNpc.npc;

import com.mojang.authlib.GameProfile;
import io.sailex.aiNpc.model.NPCState;
import io.sailex.aiNpc.network.NPCClientConnection;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.packet.c2s.common.SyncedClientOptions;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.GameMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NPCEntity extends ServerPlayerEntity {

	private static final Logger LOGGER = LoggerFactory.getLogger(NPCEntity.class);
	private final String name;
	private final GameProfile profile;
	private final NPCState state;

	public NPCEntity(String name, MinecraftServer server, ServerWorld world, GameProfile profile, NPCState state) {
		super(server, world, profile, SyncedClientOptions.createDefault());
		this.name = name;
		this.profile = profile;
		this.state = state;
	}

	public void spawnNPC() {
		LOGGER.info("Try spawning NPC: {}", this.name);
		this.server
				.getPlayerManager()
				.onPlayerConnect(
						new NPCClientConnection(NetworkSide.SERVERBOUND),
						this,
						new ConnectedClientData(profile, 0, this.getClientOptions(), false));
		LOGGER.info("NPC {} connected", this.name);

		setupNPCState();
	}

	public void setupNPCState() {
		this.teleport(
				this.getServerWorld(), state.getX(), state.getY(), state.getZ(), state.getYaw(), state.getPitch());
		this.setHealth(state.getHealth());
		this.unsetRemoved();
		this.dataTracker.set(PLAYER_MODEL_PARTS, (byte) 0x7f);
		this.interactionManager.changeGameMode(GameMode.byName(state.getGameMode()));
		this.getAbilities().flying = false;
		this.getAbilities().allowFlying = false;
	}
}
