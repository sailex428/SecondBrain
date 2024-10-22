package io.sailex.aiNpc.npc;

import com.mojang.authlib.GameProfile;
import io.sailex.aiNpc.constant.ConfigConstants;
import io.sailex.aiNpc.model.NPC;
import io.sailex.aiNpc.model.command.NPCCommand;
import io.sailex.aiNpc.pathfinding.PathFinder;
import io.sailex.aiNpc.service.ILLMService;
import io.sailex.aiNpc.service.OllamaService;
import io.sailex.aiNpc.util.FeedbackLogger;
import io.sailex.aiNpc.util.GameProfileBuilder;
import io.sailex.aiNpc.util.config.ModConfig;
import java.util.*;
import java.util.function.Supplier;
import lombok.Getter;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

public class NPCManager {

	private final GameProfileBuilder profileBuilder;

	@Getter
	private final List<NPC> npcList;

	public NPCManager() {
		this.profileBuilder = new GameProfileBuilder();
		this.npcList = new ArrayList<>();
	}

	public Supplier<Text> buildNPC(NPCCommand npcCommand, MinecraftServer server, String llmType, String llmModel) {
		String npcName = npcCommand.getName();
		GameProfile npcProfile = profileBuilder.getGameProfile(npcName, server);

		ServerWorld worldIn = server.getWorld(npcCommand.getNpcState().getDimension());

		if (npcList.size() >= ModConfig.getIntProperty(ConfigConstants.NPC_ENTITIES_MAX_COUNT)) {
			return FeedbackLogger.logError("Maximum number of NPCs reached!");
		}

		if (npcList.stream().anyMatch(npc -> npc.getId().equals(npcProfile.getId()))) {
			return FeedbackLogger.logError("NPC with that name already exists!");
		}

		for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
			if (player.getUuid().equals(npcProfile.getId())) {
				return FeedbackLogger.logError("Player with that name already exists on the server!");
			}
		}

		NPCEntity entity = new NPCEntity(npcName, server, worldIn, npcProfile, npcCommand.getNpcState());
		ILLMService llmService = new OllamaService(llmModel);
		NPCController controller = new NPCController(server, entity, llmService);
		PathFinder pathFinder = new PathFinder(worldIn, entity);

		npcList.add(new NPC(npcProfile.getId(), entity, controller, llmService, pathFinder));

		entity.connectNPC();
		controller.handleInitMessage();
		return FeedbackLogger.logInfo(String.format("NPC with name %s created!", npcName));
	}

	public Supplier<Text> removeNPC(String name, MinecraftServer server) {
		UUID npcId = profileBuilder.getGameProfile(name, server).getId();
		Optional<NPCEntity> npcEntity = npcList.stream()
				.filter(npc -> npc.getId().equals(npcId))
				.map(NPC::getNpcEntity)
				.findFirst();

		if (npcEntity.isEmpty()) {
			return FeedbackLogger.logError(String.format("Cannot find NPC with name %s", name));
		}

		npcEntity.get().removeNPC();
		npcList.removeIf(npc -> npc.getId().equals(npcId));
		return FeedbackLogger.logInfo(String.format("NPC with name %s removed!", name));
	}
}
