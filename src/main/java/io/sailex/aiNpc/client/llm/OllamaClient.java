package io.sailex.aiNpc.client.llm;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.sailex.aiNpc.client.exception.EmptyResponseException;
import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.concurrent.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class OllamaClient implements ILLMClient {

	private static final Logger LOGGER = LogManager.getLogger(OllamaClient.class);
	private static final Gson GSON = new Gson();
	private final ExecutorService service = Executors.newFixedThreadPool(1);

	private final String ollamaUrl;
	private final HttpClient httpClient;
	private final String ollamaModel;

	public OllamaClient(String ollamaModel, String ollamaUrl) {
		this.ollamaUrl = ollamaUrl;
		this.ollamaModel = ollamaModel;
		this.httpClient = HttpClient.newHttpClient();
	}

	public CompletableFuture<String> generateResponse(String userPrompt, String systemPrompt) {
		return CompletableFuture.supplyAsync(
				() -> {
					try {
						JsonObject prompt = new JsonObject();
						prompt.addProperty("user", userPrompt);
						prompt.addProperty("system", systemPrompt);

						Map<String, Object> request = Map.of(
								"model", ollamaModel,
								"prompt", prompt,
								"stream", false);
						String requestBody = GSON.toJson(request);

						HttpRequest httpRequest = HttpRequest.newBuilder()
								.uri(new URI(ollamaUrl))
								.header("Content-Type", "application/json")
								.POST(HttpRequest.BodyPublishers.ofString(requestBody))
								.build();

						HttpResponse<String> response =
								httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

						LOGGER.info("Generated response from ollama: {}", response.body());

						if (response.body() != null) {
							Map responseBody = GSON.fromJson(response.body(), Map.class);
							return (String) responseBody.get("response");
						}
						throw new EmptyResponseException("Empty response from ollama");
					} catch (IOException | InterruptedException e) {
						LOGGER.error("Error generating response from ollama", e);
						throw new CompletionException(
								new ConnectException("Failed to connect to ollama: " + e.getMessage()));
					} catch (URISyntaxException e) {
						LOGGER.error("Error generating response from ollama", e);
						throw new CompletionException(
								new URISyntaxException("Wrong ollama url syntax: ", e.getMessage()));
					}
				},
				service);
	}

	@Override
	public void stopService() {
		service.shutdown();
	}
}
