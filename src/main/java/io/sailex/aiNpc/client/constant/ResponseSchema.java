package io.sailex.aiNpc.client.constant;

import java.util.List;
import java.util.Map;

public class ResponseSchema {

	//old schemas for ollama
	public static final Map<String, Object> CHAT_MESSAGE =
			Map.of("type", "CHAT_MESSAGE", "message", "answer on the request");

	public static final Map<String, Object> MOVE =
			Map.of("type", "MOVE", "message", "answer on the request", "x", 0, "y", 0, "z", 0);

	public static final List<Map<String, Object>> ALL_SCHEMAS = List.of(CHAT_MESSAGE, MOVE);
}
