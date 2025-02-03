package me.sailex.ai.npc.llm;

import io.github.sashirestela.openai.SimpleOpenAI;
import io.github.sashirestela.openai.common.function.FunctionCall;
import io.github.sashirestela.openai.common.tool.ToolCall;
import io.github.sashirestela.openai.domain.chat.ChatMessage;
import io.github.sashirestela.openai.domain.chat.ChatRequest;
import io.github.sashirestela.openai.domain.embedding.EmbeddingFloat;
import io.github.sashirestela.openai.domain.embedding.EmbeddingRequest;
import me.sailex.ai.npc.history.ConversationHistory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * OpenAI client for generating responses.
 */
public class OpenAiClient extends ALLMClient implements ILLMClient {

	private static final String PROMPT_PREFIX = "CURRENT PROMPT: ";
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
	 * @param source 	the source of the prompt e.g. system
	 * @param prompt 	the prompt
	 * @param history  	conversation history
	 */
	@Override
	public void callFunctions(String source, String prompt, ConversationHistory history) {
		try {
			List<ChatMessage> currentMessages = new ArrayList<>();
			history.getFormattedConversation().forEach(conversation ->
					currentMessages.add(ChatMessage.SystemMessage.of(conversation)));
			currentMessages.add(buildPromptMessage(source, prompt));
			LOGGER.info("Current messages: {}", currentMessages);

            ChatMessage.ResponseMessage responseMessage;
			//execute functions until llm doesnt call anyOfThem anymore, limit to 4 iteration, maybe llm do stupid things
           	for (int i = 0; i < 4; i++) {
                ChatRequest chatRequest = ChatRequest.builder()
                        .model(openAiModel)
						.tools(functionManager.getFunctionExecutor().getToolFunctions())
                        .messages(currentMessages)
                        .build();

                responseMessage = openAiService
						.chatCompletions()
						.create(chatRequest)
						.get(5, TimeUnit.SECONDS)
						.firstMessage();
                currentMessages.add(responseMessage);

				List<ToolCall> toolCalls = responseMessage.getToolCalls();
				if (toolCalls == null || toolCalls.isEmpty()) {
					break;
				}
				ToolCall toolCall = toolCalls.getFirst();
				history.add(toolCall.getFunction().getName() + " - " + toolCall.getFunction().getArguments());
				executeFunctionCalls(toolCall, currentMessages);
            }
        } catch (Exception e) {
			LOGGER.error("Could not generate response / execute functions for prompt: {}", prompt, e);
		}
	}

	private ChatMessage buildPromptMessage(String source, String prompt) {
		String formattedPrompt = PROMPT_PREFIX + prompt;
		if (source.equals("system")) {
			return ChatMessage.SystemMessage.of(formattedPrompt);
		} else {
			return ChatMessage.UserMessage.of(formattedPrompt);
		}
	}

	private void executeFunctionCalls(ToolCall toolCall, List<ChatMessage> messages) {
		FunctionCall function = toolCall.getFunction();
		LOGGER.info("Executed function: {} : {}", function.getName(), function.getArguments());
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
					.get(5, TimeUnit.SECONDS)
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
