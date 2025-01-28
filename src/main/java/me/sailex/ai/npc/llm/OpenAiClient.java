package me.sailex.ai.npc.llm;

import io.github.sashirestela.openai.SimpleOpenAI;
import io.github.sashirestela.openai.common.function.FunctionCall;
import io.github.sashirestela.openai.common.tool.ToolCall;
import io.github.sashirestela.openai.domain.chat.ChatMessage;
import io.github.sashirestela.openai.domain.chat.ChatRequest;
import io.github.sashirestela.openai.domain.embedding.EmbeddingFloat;
import io.github.sashirestela.openai.domain.embedding.EmbeddingRequest;
import me.sailex.ai.npc.model.database.Conversation;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
	 * @param source 		 the source of the prompt e.g. system
	 * @param prompt 		 the prompt
	 * @param conversations  latest conversations of history
	 */
	@Override
	public void callFunctions(String source, String prompt, List<String> conversations) {
		try {
			List<ChatMessage> currentMessages = new ArrayList<>();
			conversations.forEach(conversation ->
					currentMessages.add(ChatMessage.SystemMessage.of(conversation)));
			currentMessages.addAll(buildPromptMessage(source, prompt));

            ChatMessage.ResponseMessage responseMessage;
			//execute functions until llm doesnt call anyOfThem anymore
			int i = 0;
            do {
                ChatRequest chatRequest = ChatRequest.builder()
                        .model(openAiModel)
						.tools(functionManager.getFunctionExecutor().getToolFunctions())
                        .messages(currentMessages)
                        .build();

                responseMessage = openAiService
						.chatCompletions()
						.create(chatRequest)
						.get(20, TimeUnit.SECONDS)
						.firstMessage();
                currentMessages.add(responseMessage);

					List<ToolCall> toolCalls = responseMessage.getToolCalls();
				if (toolCalls != null) {
                	executeFunctionCalls(toolCalls.getFirst(), currentMessages);
				}
				i++;
            } while (responseMessage.getToolCalls() != null || i > 5); //limit to 5 iteration, maybe llm do stupid things
        } catch (Exception e) {
			LOGGER.error("Could not execute functions for prompt: {}", prompt, e);
		}
	}

	private List<ChatMessage> buildPromptMessage(String source, String prompt) {
		List<ChatMessage> messages = new ArrayList<>();
		if (source.equals("system")) {
			messages.add(ChatMessage.SystemMessage.of(prompt));
		} else {
			messages.add(ChatMessage.UserMessage.of(prompt));
		}
		return messages;
	}

	private void executeFunctionCalls(ToolCall toolCall, List<ChatMessage> messages) {
		FunctionCall function = toolCall.getFunction();
		LOGGER.info("Executed function: {}", function);
		String result = functionManager.getFunctionExecutor().execute(function);
		messages.add(ChatMessage.ToolMessage.of(result, toolCall.getId()));
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
					.get(20, TimeUnit.SECONDS)
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
