package me.sailex.ai.npc.llm;

import io.github.ollama4j.OllamaAPI;
import io.github.ollama4j.exceptions.OllamaBaseException;
import io.github.ollama4j.models.chat.OllamaChatRequestBuilder;
import io.github.ollama4j.types.OllamaModelType;
import me.sailex.ai.npc.exception.OllamaNotReachableException;

import java.io.IOException;
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
	private final ExecutorService service;
	private final OllamaChatRequestBuilder builder;

	/**
	 * Constructor for OllamaClient.
	 *
	 * @param ollamaModel the ollama model (e.g. "gemma2")
	 * @param ollamaUrl   the ollama url
	 */
	public OllamaClient(String ollamaModel, String ollamaUrl) {
		this.ollamaAPI = new OllamaAPI(ollamaUrl);
		this.builder = OllamaChatRequestBuilder.getInstance(ollamaModel);
		this.service = Executors.newFixedThreadPool(3);
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

	@Override
	public String callFunctions(String userPrompt, String systemPrompt) {
		//idk if i ever support this
		return "";
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

	@Override
	public void stopService() {
		this.service.shutdown();
	}
}
