package io.sailex.aiNpc.model.llm;

import lombok.Getter;

@Getter
public class Move extends LLMResponse {

	private final String message;
	private final int x;
	private final int y;
	private final int z;

	public Move(String message, int x, int y, int z) {
		super(ResponseType.MOVE);
		this.message = message;
		this.x = x;
		this.y = y;
		this.z = z;
	}
}
