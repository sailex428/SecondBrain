package io.sailex.aiNpc.listener;

import io.sailex.aiNpc.model.NPC;
import io.sailex.aiNpc.model.ResponseSchema;
import io.sailex.aiNpc.model.event.ChatMessageEvent;
import java.util.List;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;

public class MessageListener {

	private final List<NPC> npcList;

	public MessageListener(List<NPC> npcList) {
		this.npcList = npcList;
	}

	public void register() {
		ServerMessageEvents.CHAT_MESSAGE.register((message, sender, params) -> {
			ChatMessageEvent chatMessageEvent = new ChatMessageEvent(
					message.getContent().getString(), message.getTimestamp().toString());

			npcList.forEach(npc -> npc.getNpcController().handleMessage(chatMessageEvent, ResponseSchema.CHAT_MESSAGE));
		});
	}
}
