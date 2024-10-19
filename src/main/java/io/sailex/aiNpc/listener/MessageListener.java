package io.sailex.aiNpc.listener;

import io.sailex.aiNpc.model.messageTypes.ChatMessage;
import io.sailex.aiNpc.npc.NPCController;
import io.sailex.aiNpc.npc.NPCEntity;
import java.util.Map;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;

public class MessageListener {

	private final Map<NPCEntity, NPCController> npcEntityControllerMap;

	public MessageListener(Map<NPCEntity, NPCController> npcEntityControllerMap) {
		this.npcEntityControllerMap = npcEntityControllerMap;
	}

	public void register() {
		ServerMessageEvents.CHAT_MESSAGE.register((message, sender, params) -> {
			ChatMessage chatMessage = new ChatMessage(
					message.getContent().getString(), message.getTimestamp().toString());

			npcEntityControllerMap.values().forEach(npcController -> npcController.handleChatMessage(chatMessage));
		});
	}
}
