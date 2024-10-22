package io.sailex.aiNpc.npc;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.sailex.aiNpc.constant.Instructions;
import io.sailex.aiNpc.model.event.*;
import io.sailex.aiNpc.model.event.ChatMessageEvent;
import io.sailex.aiNpc.network.RequestType;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NPCInteraction {

	private static final Gson GSON = new Gson();
	private static final Logger LOGGER = LogManager.getLogger(NPCInteraction.class);

	public static String buildRequest(NPCEvent message, Map<String, Object> schema) {
		JsonObject request = new JsonObject();
		request.add("data", GSON.toJsonTree(message));
		request.add("schema", GSON.toJsonTree(schema));

		if (message.getType().equals(RequestType.INSTRUCTION)) {
			request.addProperty("instruction", Instructions.DEFAULT_INSTRUCTION);
		}

		LOGGER.info("Built request with content: {}", request);
		return GSON.toJson(request);
	}

	public static NPCEvent parseResponse(String response) {
		try {
			JsonObject jsonResponse = GSON.fromJson(response, JsonObject.class);
			RequestType responseType =
					RequestType.valueOf(jsonResponse.get("type").getAsString());

			switch (responseType) {
				case RequestType.CHAT_MESSAGE -> {
					return GSON.fromJson(response, ChatMessageEvent.class);
				}
				default -> {
					LOGGER.error("Response type not recognized: {}", responseType);
					return null;
				}
			}

		} catch (JsonSyntaxException e) {
			LOGGER.error("Error parsing response: {}", e.getMessage());
			return null;
		}
	}
}
