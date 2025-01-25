package me.sailex.ai.npc.llm;

import io.github.sashirestela.openai.SimpleOpenAI;
import io.github.sashirestela.openai.common.function.FunctionCall;
import io.github.sashirestela.openai.common.tool.ToolCall;
import io.github.sashirestela.openai.domain.chat.ChatMessage;
import io.github.sashirestela.openai.domain.chat.ChatRequest;
import io.github.sashirestela.openai.domain.embedding.EmbeddingFloat;
import io.github.sashirestela.openai.domain.embedding.EmbeddingRequest;

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
		this.openAiModel = openAiModel;
		this.openAiService =
				SimpleOpenAI.builder().apiKey(apiKey).baseUrl(baseUrl).build();
	}

	/**
	 * Executes functions that are called by openai based on the prompt
	 *
	 * @param source the source of the prompt e.g. system
	 * @param prompt the prompt
	 */
	@Override
	public void callFunctions(String source, String prompt) {
		try {
			List<ChatMessage> messages = new ArrayList<>();
			if (source.equals("system")) {
				messages.add(ChatMessage.SystemMessage.of(prompt));
			} else {
				messages.add(ChatMessage.UserMessage.of(prompt));
			}
            ChatMessage.ResponseMessage responseMessage;

			//execute functions until llm doesnt call anyOfThem anymore
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
			LOGGER.error("Could not execute functions for prompt: {}", prompt, e);
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
