package io.sailex.aiNpc.npc;

import com.mojang.authlib.GameProfile;
import io.sailex.aiNpc.model.command.NPCState;
import io.sailex.aiNpc.network.NPCClientConnection;
import io.sailex.aiNpc.pathfinding.PathFinder;
import io.sailex.aiNpc.pathfinding.PathNode;
import io.sailex.aiNpc.util.ChatUtils;
import java.util.List;
import lombok.Getter;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.packet.c2s.common.SyncedClientOptions;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NPCEntity extends ServerPlayerEntity {

	private static final Logger LOGGER = LoggerFactory.getLogger(NPCEntity.class);

	@Getter
	private final String npcName;

	private final GameProfile profile;
	private final NPCState state;
	private final PathFinder pathFinder;

	public NPCEntity(String npcName, MinecraftServer server, ServerWorld world, GameProfile profile, NPCState state) {
		super(server, world, profile, SyncedClientOptions.createDefault());
		this.npcName = npcName;
		this.profile = profile;
		this.state = state;
		this.pathFinder = new PathFinder(world, this);
	}

	public void connectNPC() {
		LOGGER.info("Try spawning NPC: {}", this.npcName);
		this.server
				.getPlayerManager()
				.onPlayerConnect(
						new NPCClientConnection(NetworkSide.SERVERBOUND),
						this,
						new ConnectedClientData(profile, 0, this.getClientOptions()));
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
		this.server.getPlayerManager().broadcast(ChatUtils.format(message, npcName), false);
	}

	public void moveTo(int x, int y, int z) {
		List<PathNode> pathNodes = pathFinder.findPath(this.getBlockPos(), new BlockPos(x, y, z));
		pathFinder.executeMovement(pathNodes);
	}
}
