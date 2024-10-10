package io.sailex.aiNpc.service;

import com.google.gson.Gson;
import io.sailex.aiNpc.exception.EmptyResponseException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class OllamaService implements ILLMService {
	private static final Logger LOGGER = LogManager.getLogger(OllamaService.class);
	private static final String OLLAMA_PATH = "/api/generate";

	private final String ollamaUrl;
	private final HttpClient httpClient;
	private final String ollamaModel;
	private final Gson gson;

	public OllamaService(String ollamaUrl, String ollamaModel) {
		this.ollamaUrl = ollamaUrl;
		this.ollamaModel = ollamaModel;
		this.httpClient = HttpClient.newHttpClient();
		this.gson = new Gson();
	}

	public String generateResponse(String prompt) throws EmptyResponseException {
		try {
			Map<String, Object> request = Map.of("model", ollamaModel, "prompt", prompt, "stream", false);
			String requestBody = gson.toJson(request);

			HttpRequest httpRequest = HttpRequest.newBuilder()
					.uri(new URI(ollamaUrl + OLLAMA_PATH))
					.header("Content-Type", "application/json")
					.POST(HttpRequest.BodyPublishers.ofString(requestBody))
					.build();

			HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

			if (response.body() != null) {
				Map responseBody = gson.fromJson(response.body(), Map.class);
				return (String) responseBody.get("response");
			}
			throw new EmptyResponseException("Empty response from Ollama");
		} catch (Exception e) {
			LOGGER.error("Error generating response from Ollama", e);
			throw new RuntimeException("Failed to generate response", e);
		}
	}
}
