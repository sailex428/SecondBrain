package io.sailex.aiNpc.npc;

import com.google.gson.*;
import io.sailex.aiNpc.constant.Instructions;
import io.sailex.aiNpc.constant.ResponseSchema;
import io.sailex.aiNpc.model.NPCEvent;
import io.sailex.aiNpc.model.context.WorldContext;
import io.sailex.aiNpc.model.llm.ChatMessage;
import io.sailex.aiNpc.model.llm.LLMResponse;
import io.sailex.aiNpc.model.llm.Move;
import io.sailex.aiNpc.model.llm.ResponseType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NPCInteraction {

	private static final Gson GSON = new Gson();
	private static final Logger LOGGER = LogManager.getLogger(NPCInteraction.class);

	public static String buildRequest(NPCEvent message, WorldContext context) {
		JsonObject request = new JsonObject();

		JsonArray dataArray = new JsonArray();
		dataArray.add(GSON.toJsonTree(context));
		dataArray.add(GSON.toJsonTree(message));

		request.add("data", dataArray);
		request.add("schema", GSON.toJsonTree(ResponseSchema.ALL_SCHEMAS));
		request.add("instruction", GSON.toJsonTree(Instructions.STRUCTURE_INSTRUCTIONS));

		LOGGER.info("Built request with content: {}", request);
		return GSON.toJson(request);
	}

	public static LLMResponse parseResponse(String response) {
		try {
			JsonArray jsonResponse = GSON.fromJson(response, JsonArray.class);

			for (JsonElement responseElement : jsonResponse) {
				JsonObject jsonObject = responseElement.getAsJsonObject();
				ResponseType responseType =
						ResponseType.valueOf(jsonObject.get("type").getAsString());
				switch (responseType) {
					case ResponseType.CHAT_MESSAGE -> {
						return GSON.fromJson(responseElement, ChatMessage.class);
					}
					case ResponseType.MOVE -> {
						return GSON.fromJson(responseElement, Move.class);
					}
					default -> {
						LOGGER.error("Response type not recognized: {}", responseType);
						throw new IllegalArgumentException("Response type not recognized: " + responseType);
					}
				}
			}
			return new LLMResponse(ResponseType.EMPTY);

		} catch (JsonSyntaxException e) {
			LOGGER.error("Error parsing response: {}", e.getMessage());
			throw new JsonSyntaxException("Error parsing response: " + e.getMessage());
		}
	}
}
