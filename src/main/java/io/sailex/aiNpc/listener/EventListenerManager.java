package io.sailex.aiNpc.listener;

import io.sailex.aiNpc.model.NPC;
import io.sailex.aiNpc.model.NPCEvent;
import io.sailex.aiNpc.model.llm.RequestType;
import java.util.List;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.minecraft.util.ActionResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EventListenerManager {

	private final List<NPC> npcList;
	private static final Logger LOGGER = LogManager.getLogger(EventListenerManager.class);

	public EventListenerManager(List<NPC> npcList) {
		this.npcList = npcList;
	}

	public void register() {
		registerBlockInteractionListener();
		registerChatMessageListener();
		registerServerStoppingListener();
		//		registerEntityLoadListener();
	}

	private void registerServerStoppingListener() {
		ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
			npcList.forEach(npc -> npc.getLlmService().stopService());
			npcList.forEach(npc -> npc.getNpcContextGenerator().stopService());
		});
	}

	private void registerChatMessageListener() {
		ServerMessageEvents.CHAT_MESSAGE.register((message, sender, params) -> {
			String chatMessage = String.format(
					"%s : The player %s has written the message: %s",
					message.getTimestamp().toString(),
					sender.getName().getString(),
					message.getContent().getString());

			handleMessage(RequestType.CHAT_MESSAGE, chatMessage);
		});
	}

	private void registerBlockInteractionListener() {
		PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, entity) -> {
			LOGGER.info("Player {} broke block at {}", player.getName().getString(), pos.toString());
			String blockBreakMessage = String.format(
					"Player %s broke block at %s", player.getName().getString(), pos.toShortString());
			handleMessage(RequestType.BLOCK_INTERACTION, blockBreakMessage);
		});

		UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
			LOGGER.info(
					"Player {} interacted with block at {}",
					player.getName().getString(),
					hitResult.getBlockPos().toString());
			String blockInteractionMessage = String.format(
					"Player %s interacted with block at %s",
					player.getName().getString(), hitResult.getBlockPos().toShortString());
			handleMessage(RequestType.BLOCK_INTERACTION, blockInteractionMessage);
			return ActionResult.PASS;
		});
	}

	private void registerEntityLoadListener() {
		ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
			if (npcList.stream().anyMatch(npc -> npc.getId().equals(entity.getUuid()))) {
				return;
			}
			LOGGER.info(
					"Entity {} loaded in world {}",
					entity.getName().getString(),
					world.getRegistryKey().getValue());
			String entityLoadMessage = String.format(
					"Entity %s loaded in world %s",
					entity.getName().getString(), world.getRegistryKey().getValue());
			handleMessage(RequestType.ENTITY_LOAD, entityLoadMessage);
		});
	}

	private void handleMessage(RequestType type, String message) {
		npcList.forEach(npc -> npc.getNpcController().handleMessage(new NPCEvent(type, message)));
	}
}
