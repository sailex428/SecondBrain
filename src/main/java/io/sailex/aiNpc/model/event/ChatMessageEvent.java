package io.sailex.aiNpc.model.event;

import io.sailex.aiNpc.network.RequestType;
import lombok.Getter;

@Getter
public class ChatMessageEvent extends NPCEvent {

	private final String message;
	private final String timestamp;

	public ChatMessageEvent(String message, String timestamp) {
		super(RequestType.CHAT_MESSAGE);
		this.message = message;
		this.timestamp = timestamp;
	}
}
