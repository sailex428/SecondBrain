package me.sailex.ai.npc.llm;

import io.github.ollama4j.OllamaAPI;
import io.github.ollama4j.exceptions.OllamaBaseException;
import io.github.ollama4j.models.chat.OllamaChatMessageRole;
import io.github.ollama4j.models.chat.OllamaChatRequest;
import io.github.ollama4j.models.chat.OllamaChatRequestBuilder;
import io.github.ollama4j.types.OllamaModelType;
import me.sailex.ai.npc.exception.OllamaNotReachableException;
import me.sailex.ai.npc.model.interaction.Skill;

import java.io.IOException;
import java.net.ConnectException;
import java.util.List;
import java.util.concurrent.*;
import lombok.Setter;
import me.sailex.ai.npc.util.LogUtil;

/**
 * Ollama client for generating responses.
 */
public class OllamaClient extends ALLMClient implements ILLMClient {

	@Setter
	private OllamaAPI ollamaAPI;

	private final OllamaChatRequestBuilder builder;

	/**
	 * Constructor for OllamaClient.
	 *
	 * @param ollamaModel the ollama model (e.g. "gemma2")
	 * @param ollamaUrl   the ollama url
	 */
	public OllamaClient(String ollamaModel, String ollamaUrl) {
		super();
		this.ollamaAPI = new OllamaAPI(ollamaUrl);
		this.builder = OllamaChatRequestBuilder.getInstance(ollamaModel);
	}

	/**
	 * Check if the service is reachable
	 * @throws RuntimeException if server is not reachable
	 */
	@Override
	public void checkServiceIsReachable() {
		try {
			boolean isOllamaServerReachable = ollamaAPI.ping();
			if (!isOllamaServerReachable) {
				LogUtil.error("Ollama server is not reachable");
			}
		} catch (RuntimeException e) {
			String errorMsg = "Ollama server is not reachable";
			LogUtil.error(errorMsg);
			throw new OllamaNotReachableException(errorMsg);
		}
	}

	/**
	 * Generate response from Ollama.
	 *
	 * @param userPrompt the user prompt
	 * @param systemPrompt the system prompt
	 * @return the llm response
	 */
	@Override
	public String generateResponse(String userPrompt, String systemPrompt) {
		return CompletableFuture.supplyAsync(
						() -> {
							try {
								OllamaChatRequest requestModel = builder.withMessage(
												OllamaChatMessageRole.SYSTEM, systemPrompt)
										.withMessage(OllamaChatMessageRole.USER, userPrompt)
										.withResponseClass(Skill.class)
										.build();
								ollamaAPI.setRequestTimeoutSeconds(30);

								return ollamaAPI.chat(requestModel).getResponse();
							} catch (IOException | InterruptedException | OllamaBaseException e) {
								LOGGER.error("Error generating response from ollama", e);
								Thread.currentThread().interrupt();
								throw new CompletionException(new ConnectException(
										"Error generating response from ollama: " + e.getMessage()));
							}
						},
						service)
				.exceptionally(exception -> {
					LogUtil.error(exception.getMessage());
					return null;
				})
				.join();
	}

	@Override
	public double[] generateEmbedding(List<String> prompt) {
		return CompletableFuture.supplyAsync(
						() -> {
							try {
								return convertEmbedding(ollamaAPI
										.embed(OllamaModelType.NOMIC_EMBED_TEXT, prompt)
										.getEmbeddings());
							} catch (IOException | OllamaBaseException | InterruptedException e) {
								Thread.currentThread().interrupt();
								throw new CompletionException(
										"Error generating embedding for prompt: " + prompt.getFirst(), e);
							}
						},
						service)
				.exceptionally(exception -> {
					LOGGER.error(exception.getMessage());
					return new double[] {};
				})
				.join();
	}
}
