package me.sailex.ai.npc.llm;

import io.github.sashirestela.openai.SimpleOpenAI;
import io.github.sashirestela.openai.common.function.FunctionCall;
import io.github.sashirestela.openai.common.function.FunctionDef;
import io.github.sashirestela.openai.common.function.FunctionExecutor;
import io.github.sashirestela.openai.common.tool.ToolCall;
import io.github.sashirestela.openai.domain.chat.ChatMessage;
import io.github.sashirestela.openai.domain.chat.ChatRequest;
import io.github.sashirestela.openai.domain.embedding.EmbeddingFloat;
import io.github.sashirestela.openai.domain.embedding.EmbeddingRequest;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class OpenAiClient extends ALLMClient<FunctionDef> {

	private final SimpleOpenAI openAiService;
	private final String openAiModel;

	private final FunctionExecutor functionExecutor;

	/**
	 * Constructor for OpenAiClient.
	 *
	 * @param apiKey  the api key
	 */
	public OpenAiClient(
			String apiKey,
			String baseUrl
	) {
		this.openAiModel = "gpt-4o-mini";
		this.openAiService =
				SimpleOpenAI.builder().apiKey(apiKey).baseUrl(baseUrl).build();
		this.functionExecutor = new FunctionExecutor();
	}

	/**
	 * Executes functions that are called by openai based on the prompt and registered functions.
	 *
	 * @param 	prompt 	   the prompt
	 * @param 	functions  functions relevant functions that matches to the prompt
	 * @return  the formatted results of the function calls.
	 */
	@Override
	public String callFunctions(String prompt, List<FunctionDef> functions) {
		try {
			StringBuilder calledFunctions = new StringBuilder();

            ChatMessage.ResponseMessage responseMessage;
           	for (int i = 0; i < functions.size(); i++) {
				functionExecutor.enrollFunctions(functions);
                ChatRequest chatRequest = ChatRequest.builder()
                        .model(openAiModel)
						.tools(functionExecutor.getToolFunctions())
                        .message(ChatMessage.UserMessage.of(prompt))
                        .build();

                responseMessage = openAiService
						.chatCompletions()
						.create(chatRequest)
						.get(10, TimeUnit.SECONDS)
						.firstMessage();

				List<ToolCall> toolCalls = responseMessage.getToolCalls();
				if (toolCalls == null || toolCalls.isEmpty()) {
					break;
				}
				ToolCall toolCall = toolCalls.getFirst();
				calledFunctions.append(toolCall.getFunction().getName())
						.append(" - args: ")
						.append(toolCall.getFunction().getArguments())
						.append(StringUtils.SPACE);
				executeFunctionCalls(toolCall);
				removeCalledFunctions(functions, toolCall.getFunction().getName());
            }
			return calledFunctions.toString();
        } catch (Exception e) {
			LOGGER.error("Could not generate response / execute functions for prompt: {}", prompt, e);
			Thread.currentThread().interrupt();
			return StringUtils.EMPTY;
		}
	}

	private void removeCalledFunctions(List<FunctionDef> functions, String functionName) {
		List<FunctionDef> functionsToRemove = new ArrayList<>();
		functions.forEach(func -> {
			if (func.getName().equals(functionName)) {
				functionsToRemove.add(func);
			}
		});
		functions.removeAll(functionsToRemove);
	}

	private void executeFunctionCalls(ToolCall toolCall) {
		FunctionCall function = toolCall.getFunction();
		LOGGER.info("Executed function: {} - {}", function.getName(), function.getArguments());
		functionExecutor.execute(function);
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
					.get(10, TimeUnit.SECONDS)
					.getData()
					.stream()
					.map(EmbeddingFloat::getEmbedding)
					.toList());
		} catch (Exception e) {
			Thread.currentThread().interrupt();
			LOGGER.error("Could not generate embedding for prompt: {}", prompt.getFirst(), e);
			return new double[] {};
		}
	}

}
