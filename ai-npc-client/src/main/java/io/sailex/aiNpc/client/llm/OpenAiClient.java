package io.sailex.aiNpc.client.llm;

import io.github.sashirestela.openai.SimpleOpenAI;
import io.github.sashirestela.openai.common.ResponseFormat;
import io.github.sashirestela.openai.domain.chat.ChatMessage;
import io.github.sashirestela.openai.domain.chat.ChatRequest;
import io.github.sashirestela.openai.domain.embedding.EmbeddingFloat;
import io.github.sashirestela.openai.domain.embedding.EmbeddingRequest;
import io.sailex.aiNpc.client.exception.EmptyResponseException;
import io.sailex.aiNpc.client.model.interaction.Actions;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * OpenAI client for generating responses.
 */
public class OpenAiClient extends ALLMClient implements ILLMClient {

	private final String openAiModel;
	private final SimpleOpenAI openAiService;

	/**
	 * Constructor for OpenAiClient.
	 *
	 * @param openAiModel the openai model (e.g. "gpt-3.5-turbo")
	 * @param apiKey      the api key
	 */
	public OpenAiClient(String openAiModel, String apiKey) {
		super();
		this.openAiModel = openAiModel;
		this.openAiService = SimpleOpenAI.builder().apiKey(apiKey).build();
	}

	/**
	 * Generate response from OpenAI.
	 *
	 * @param userPrompt   the user prompt
	 * @param systemPrompt the system prompt
	 * @return the llm response
	 */
	@Override
	public CompletableFuture<String> generateResponse(String userPrompt, String systemPrompt) {
		return CompletableFuture.supplyAsync(
				() -> {
					ChatRequest chatRequest = ChatRequest.builder()
							.model(openAiModel)
							.message(ChatMessage.SystemMessage.of(systemPrompt))
							.message(ChatMessage.UserMessage.of(userPrompt))
							.responseFormat(ResponseFormat.jsonSchema(ResponseFormat.JsonSchema.builder()
									.name("Actions")
									.schemaClass(Actions.class)
									.build()))
							.build();

					String chatResponse = openAiService
							.chatCompletions()
							.create(chatRequest)
							.join()
							.firstContent();

					LOGGER.info("Generated response from openai: {}", chatResponse);
					if (!chatResponse.isBlank()) {
						return chatResponse;
					}
					throw new EmptyResponseException("Empty response from openai");
				},
				service);
	}

	@Override
	public Float[] generateEmbedding(List<String> prompt) {
		List<List<Double>> embedding = openAiService
				.embeddings()
				.create(EmbeddingRequest.builder().input(prompt).build()).
				join()
				.getData()
				.stream()
				.map(EmbeddingFloat::getEmbedding)
				.toList();
		return convertEmbedding(embedding);
	}
}
