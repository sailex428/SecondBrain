package io.sailex.aiNpc.model;

import java.util.Map;

public class ResponseSchema {

	public static final Map<String, Object> CHAT_MESSAGE =
			Map.of("type", "CHAT_MESSAGE", "message", "answer on the request");
}
