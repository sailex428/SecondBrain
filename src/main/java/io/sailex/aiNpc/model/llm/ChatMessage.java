package io.sailex.aiNpc.model.llm;

import lombok.Getter;

@Getter
public class ChatMessage extends LLMResponse {

	private final String message;

	public ChatMessage(String message) {
		super(ResponseType.CHAT_MESSAGE);
		this.message = message;
	}
}
