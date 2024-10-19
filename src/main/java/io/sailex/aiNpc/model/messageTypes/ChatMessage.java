package io.sailex.aiNpc.model.messageTypes;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatMessage extends Message {

	private String message;
	private String timestamp;

	public ChatMessage(String message, String timestamp) {
		super(RequestType.CHAT_MESSAGE);
		this.message = message;
		this.timestamp = timestamp;
	}
}
