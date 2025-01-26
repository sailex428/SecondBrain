package me.sailex.ai.npc.llm;

import io.github.sashirestela.openai.SimpleOpenAI;
import io.github.sashirestela.openai.common.ResponseFormat;
import io.github.sashirestela.openai.common.function.FunctionCall;
import io.github.sashirestela.openai.common.tool.ToolCall;
import io.github.sashirestela.openai.domain.chat.ChatMessage;
import io.github.sashirestela.openai.domain.chat.ChatRequest;
import io.github.sashirestela.openai.domain.embedding.EmbeddingFloat;
import io.github.sashirestela.openai.domain.embedding.EmbeddingRequest;
import me.sailex.ai.npc.exception.EmptyResponseException;
import me.sailex.ai.npc.model.interaction.Skill;

import java.util.ArrayList;
import java.util.List;

/**
 * OpenAI client for generating responses.
 */
public class OpenAiClient extends ALLMClient implements ILLMClient {

	private final SimpleOpenAI openAiService;
	private final String openAiModel;

	/**
	 * Constructor for OpenAiClient.
	 *
	 * @param openAiModel the openai model (e.g. "gpt-3.5-turbo")
	 * @param apiKey      the api key
	 */
	public OpenAiClient(
			String openAiModel,
			String apiKey,
			String baseUrl
	) {
		super();
		this.openAiModel = openAiModel;
		this.openAiService =
				SimpleOpenAI.builder().apiKey(apiKey).baseUrl(baseUrl).build();
	}

	/**
	 * Executes functions that are called by openai
	 *
	 * @param userPrompt 	the prompt from the user
	 * @param systemPrompt	the prompt from the system/game
	 */
	@Override
	public void callFunctions(String userPrompt, String systemPrompt) {
		try {
            ChatMessage.ResponseMessage responseMessage;
			List<ChatMessage> messages = new ArrayList<>();
			messages.add(ChatMessage.SystemMessage.of(systemPrompt));
			messages.add(ChatMessage.UserMessage.of(userPrompt));

			//execute functions until llm doesnt call them anymore
            do {
                ChatRequest chatRequest = ChatRequest.builder()
                        .model(openAiModel)
                        .messages(messages)
                        .build();

                responseMessage = openAiService.chatCompletions().create(chatRequest).join().firstMessage();
                messages.add(responseMessage);

                executeFunctionCalls(responseMessage.getToolCalls(), messages);
            } while (!responseMessage.getToolCalls().isEmpty());

        } catch (Exception e) {
			LOGGER.error("Could not execute functions for prompt: {}", userPrompt, e);
		}
	}

	private void executeFunctionCalls(List<ToolCall> toolCalls, List<ChatMessage> messages) {
		toolCalls.forEach(toolCall -> {
			FunctionCall function = toolCall.getFunction();
			LOGGER.info("Executed function: {}", function);
			String result = functionManager.getFunctionExecutor().execute(function);
			messages.add(ChatMessage.ToolMessage.of(result, toolCall.getId()));
		});
	}

	/**
	 * {@code @Deprecated}
	 * Generate response with openai
	 *
	 * @param userPrompt   the user prompt
	 * @param systemPrompt the system prompt
	 * @return the llm response
	 */
	@Override
	public String generateResponse(String userPrompt, String systemPrompt) {
		try {
			ChatRequest chatRequest = ChatRequest.builder()
					.model(openAiModel)
					.message(ChatMessage.SystemMessage.of(systemPrompt))
					.message(ChatMessage.UserMessage.of(userPrompt))
					.responseFormat(ResponseFormat.jsonSchema(ResponseFormat.JsonSchema.builder()
							.name("Actions")
							.schemaClass(Skill.class)
							.build()))
					.build();

			String chatResponse =
					openAiService.chatCompletions().create(chatRequest).join().firstContent();

			LOGGER.info("Generated response from openai: {}", chatResponse);
			if (!chatResponse.isBlank()) {
				return chatResponse;
			}
			throw new EmptyResponseException("Empty response from openai");
		} catch (Exception e) {
			LOGGER.error("Could not generate response for prompt: {}", userPrompt, e);
			return null;
		}
	}

	@Override
	public double[] generateEmbedding(List<String> prompt) {
		try {
			return convertEmbedding(openAiService
					.embeddings()
					.create(EmbeddingRequest.builder()
							.model("text-embedding-3-small")
							.input(prompt)
							.build())
					.join()
					.getData()
					.stream()
					.map(EmbeddingFloat::getEmbedding)
					.toList());
		} catch (Exception e) {
			LOGGER.error("Could not generate embedding for prompt: {}", prompt.getFirst(), e);
			return new double[] {};
		}
	}
}
