package io.sailex.aiNpc.npc;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.sailex.aiNpc.model.messageTypes.ChatMessage;
import io.sailex.aiNpc.model.messageTypes.InstructionMessage;
import io.sailex.aiNpc.model.messageTypes.Message;
import io.sailex.aiNpc.model.messageTypes.RequestType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NPCInteraction {

	private static final Gson GSON = new Gson();
	private static final Logger LOGGER = LogManager.getLogger(NPCInteraction.class);

	public static String buildRequest(Message message) {
		JsonObject request = new JsonObject();
		request.addProperty("format_version", "1.0");

		RequestType requestType = message.getRequestType();
		request.addProperty("type", requestType.name().toLowerCase());

		switch (message.getRequestType()) {
			case CHAT_MESSAGE -> buildChatMessageRequest((ChatMessage) message, request);
			case INSTRUCTION -> buildInstructionMessageRequest((InstructionMessage) message, request);
			default -> LOGGER.error("Request type not recognized: {}", requestType);
		}

		LOGGER.info("Built request with content: {}", request);
		return GSON.toJson(request);
	}

	private static void buildChatMessageRequest(ChatMessage chatMessage, JsonObject request) {
		request.add("message", GSON.toJsonTree(chatMessage.getMessage()));
		request.add("timestamp", GSON.toJsonTree(chatMessage.getTimestamp()));
	}

	private static void buildInstructionMessageRequest(InstructionMessage instructionMessage, JsonObject request) {
		request.add("instruction", GSON.toJsonTree(instructionMessage.getInstruction()));
	}

	public static Message parseResponse(String response) {
		JsonObject jsonResponse = GSON.fromJson(response, JsonObject.class);

		String responseType = jsonResponse.get("type").getAsString();
		Message message = null;

		switch (responseType) {
			case "chat_message" -> message = GSON.fromJson(response, ChatMessage.class);
			case "instruction" -> message = GSON.fromJson(response, InstructionMessage.class);
			default -> LOGGER.error("Response type not recognized: {}", responseType);
		}
		LOGGER.info("Parsed response with content: {}", message);
		return message;
	}
}
