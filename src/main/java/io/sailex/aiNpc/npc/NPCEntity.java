package io.sailex.aiNpc.npc;

import com.mojang.authlib.GameProfile;
import io.sailex.aiNpc.model.command.NPCState;
import io.sailex.aiNpc.network.NPCClientConnection;
import lombok.Getter;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.packet.c2s.common.SyncedClientOptions;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.world.GameMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NPCEntity extends ServerPlayerEntity {

	private static final Logger LOGGER = LoggerFactory.getLogger(NPCEntity.class);

	@Getter
	private final String npcName;

	private final GameProfile profile;
	private final NPCState state;

	public NPCEntity(String npcName, MinecraftServer server, ServerWorld world, GameProfile profile, NPCState state) {
		super(server, world, profile, SyncedClientOptions.createDefault());
		this.npcName = npcName;
		this.profile = profile;
		this.state = state;
	}

	public void connectNPC() {
		LOGGER.info("Try spawning NPC: {}", this.npcName);
		this.server
				.getPlayerManager()
				.onPlayerConnect(
						new NPCClientConnection(NetworkSide.SERVERBOUND),
						this,
						new ConnectedClientData(profile, 0, this.getClientOptions(), false));
		LOGGER.info("NPC {} connected", this.npcName);

		setupNPCState();
	}

	public void setupNPCState() {
		this.teleport(
				this.getServerWorld(),
				state.getX(),
				state.getY(),
				state.getZ(),
				state.getBodyYaw(),
				state.getHeadPitch());
		this.setHealth(state.getHealth());
		this.unsetRemoved();
		this.dataTracker.set(PLAYER_MODEL_PARTS, (byte) 0x7f);
		this.interactionManager.changeGameMode(GameMode.byName(state.getGameMode()));
		this.getAbilities().flying = false;
		this.getAbilities().allowFlying = false;
	}

	public void removeNPC() {
		LOGGER.info("Try removing NPC: {}", this.npcName);
		this.server.getPlayerManager().remove(this);
		this.networkHandler.disconnect(Text.of("NPC removed"));
		this.discard();
		LOGGER.info("NPC {} removed", this.npcName);
	}

	public void sendChatMessage(String message) {
		this.sendMessage(Text.literal(message));
		this.server.getPlayerManager().broadcast(Text.of(message), false);
	}
}
