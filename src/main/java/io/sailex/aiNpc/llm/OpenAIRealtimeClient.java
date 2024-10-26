package io.sailex.aiNpc.llm;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.sailex.aiNpc.constant.ConfigConstants;
import io.sailex.aiNpc.constant.Instructions;
import io.sailex.aiNpc.constant.ResponseSchema;
import io.sailex.aiNpc.util.config.ModConfig;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

public class OpenAIRealtimeClient extends WebSocketClient {

	private static final Logger LOGGER = LogManager.getLogger(OpenAIRealtimeClient.class);
	private static final Gson GSON = new Gson();

	private final MessageHandler messageHandler;

	public OpenAIRealtimeClient(String model, MessageHandler messageHandler) {
		super(createURI(model), createHeaders());
		this.messageHandler = messageHandler;

		this.setConnectionLostTimeout(30);
		this.connect();
	}

	private static URI createURI(String model) {
		String baseUrl = ModConfig.getProperty(ConfigConstants.NPC_LLM_OPENAI_URL);
		try {
			String uri = baseUrl + "?model=" + model;
			LOGGER.info("Connecting to OpenAI realtime API: {}", uri);
			return new URI(uri);
		} catch (URISyntaxException e) {
			LOGGER.error("Invalid URI {}", e.getMessage());
			throw new RuntimeException("Invalid URI", e);
		}
	}

	private static Map<String, String> createHeaders() {
		String apiKey = ModConfig.getProperty(ConfigConstants.NPC_LLM_OPENAI_API_KEY);
		Map<String, String> headers = new HashMap<>();
		headers.put("Authorization", "Bearer " + apiKey);
		headers.put("OpenAI-Beta", "realtime=v1");
		return headers;
	}

	@Override
	public void onOpen(ServerHandshake handshake) {
		LOGGER.info("Connected to OpenAI realtime API: {}", handshake.getHttpStatus());
		this.sendMessageWithSchema(Instructions.DEFAULT_INSTRUCTION, ResponseSchema.CHAT_MESSAGE);
	}

	@Override
	public void onClose(int code, String reason, boolean remote) {
		LOGGER.info("Connection closed: {} - {}", code, reason);
	}

	@Override
	public void onError(Exception e) {
		LOGGER.error("WebSocket error occurred", e);
	}

	public void sendMessageWithSchema(String message, Map<String, Object> schema) {
		sendMessage(message, Map.of("type", "json_schema", "json_schema", Map.of("strict", true, "schema", schema)));
	}

	public void sendMessage(String message) {
		sendMessage(message, Map.of("type", "json_object"));
	}

	private void sendMessage(String message, Map<String, Object> responseFormat) {
		Map<String, Object> messageMap = new HashMap<>();
		messageMap.put("type", "response.create");
		messageMap.put("modalities", List.of("text"));
		messageMap.put("response", Map.of("instruction", message));
		messageMap.put("required", List.of("message"));
		messageMap.put("response_format", responseFormat);
		send(GSON.toJson(messageMap));
	}

	@Override
	public void onMessage(String message) {
		try {
			JsonObject parsedMessage = GSON.fromJson(message, JsonObject.class);
			LOGGER.info("Received message: {}", parsedMessage);
			this.messageHandler.onMessage(parsedMessage.getAsJsonObject("response"));
		} catch (Exception e) {
			LOGGER.error("Error processing message: {}", message, e);
		}
	}

	@FunctionalInterface
	public interface MessageHandler {
		void onMessage(JsonObject message);
	}
}
