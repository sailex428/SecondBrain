package io.sailex.ai.npc.client.llm;

import io.github.ollama4j.OllamaAPI;
import io.github.ollama4j.exceptions.OllamaBaseException;
import io.github.ollama4j.models.chat.OllamaChatMessageRole;
import io.github.ollama4j.models.chat.OllamaChatRequest;
import io.github.ollama4j.models.chat.OllamaChatRequestBuilder;
import io.github.ollama4j.types.OllamaModelType;
import io.sailex.ai.npc.client.constant.Instructions;
import io.sailex.ai.npc.client.util.LogUtil;
import lombok.Setter;

import java.io.IOException;
import java.net.ConnectException;
import java.util.List;
import java.util.concurrent.*;

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

	@Override
	public void checkServiceIsReachable() {
		boolean isOllamaServerReachable = ollamaAPI.ping();
		if (!isOllamaServerReachable) {
			LogUtil.error("Ollama server is not reachable");
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
										OllamaChatMessageRole.SYSTEM,
										systemPrompt + Instructions.STRUCTURE_INSTRUCTIONS)
								.withMessage(OllamaChatMessageRole.USER, userPrompt)
								.build();
						//allow only json output
						requestModel.setReturnFormatJson(true);
						ollamaAPI.setRequestTimeoutSeconds(30);

						return ollamaAPI.chat(requestModel).getResponse();
					} catch (IOException | InterruptedException | OllamaBaseException e) {
						LOGGER.error("Error generating response from ollama", e);
						throw new CompletionException(
								new ConnectException("Error generating response from ollama: " + e.getMessage()));
					}
				},
				service).exceptionally(exception -> {
					LogUtil.error(exception.getMessage());
					return null;
		}).join();
	}

	@Override
	public double[] generateEmbedding(List<String> prompt) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				return convertEmbedding(ollamaAPI.embed(OllamaModelType.NOMIC_EMBED_TEXT, prompt).getEmbeddings());
			} catch (IOException | InterruptedException | OllamaBaseException e) {
				throw new CompletionException("Error generating embedding for prompt: " + prompt.getFirst(), e);
			}
		}, service).exceptionally(exception -> {
				LogUtil.error(exception.getMessage());
				return null;
			})
		.join();
    }
}
