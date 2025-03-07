package me.sailex.ai.npc.llm;

import com.fasterxml.jackson.core.JacksonException;
import io.github.ollama4j.OllamaAPI;
import io.github.ollama4j.models.chat.*;
import io.github.ollama4j.models.embeddings.OllamaEmbedResponseModel;
import io.github.ollama4j.tools.OllamaToolCallsFunction;
import io.github.ollama4j.tools.Tools;
import io.github.ollama4j.types.OllamaModelType;
import me.sailex.ai.npc.exception.LLMServiceException;

import java.util.List;
import java.util.concurrent.*;

import lombok.Setter;
import me.sailex.ai.npc.util.LogUtil;
import org.apache.commons.lang3.StringUtils;

public class OllamaClient extends ALLMClient<Tools.ToolSpecification> {

	@Setter
	private OllamaAPI ollamaAPI;
	private final ExecutorService service;
	private final String model;

	/**
	 * Constructor for OllamaClient.
	 *
	 * @param url  the ollama url
	 */
	public OllamaClient(String url) {
		this.ollamaAPI = new OllamaAPI(url);
		this.model = "llama3.2:3b";
		this.service = Executors.newFixedThreadPool(3);
		ollamaAPI.setMaxChatToolCallRetries(4);
		ollamaAPI.setVerbose(true);
		ollamaAPI.setRequestTimeoutSeconds(20);
	}

	/**
	 * Check if the service is reachable
	 * @throws LLMServiceException if server is not reachable
	 */
	@Override
	public void checkServiceIsReachable() {
		try {
			boolean isOllamaServerReachable = ollamaAPI.ping();
			if (!isOllamaServerReachable) {
				LogUtil.error("Ollama server is not reachable");
			}
		} catch (Exception e) {
			String errorMsg = "Ollama server is not reachable";
			LogUtil.error(errorMsg);
			throw new LLMServiceException(errorMsg);
		}
	}

	public void registerFunctions(List<Tools.ToolSpecification> tools) {
		tools.forEach(ollamaAPI::registerTool);
	}

	/**
	 * Sends the provided prompt and functions to Ollama API.
	 * Executes functions called by the LLM.
	 *
	 * @param   prompt    the event prompt
	 * @param   functions relevant functions that matches to the prompt
	 * @return  the formatted results of the function calls.
	 */
	@Override
	public String callFunctions(String prompt, List<Tools.ToolSpecification> functions) {
		try {
			ollamaAPI.registerTools(functions);

			OllamaChatRequest toolRequest = OllamaChatRequestBuilder.getInstance(model)
				.withMessage(OllamaChatMessageRole.USER, prompt)
				.build();
			OllamaChatResult response = ollamaAPI.chat(toolRequest);

			return formatChatHistory(response.getChatHistory());
		} catch (JacksonException e) {
			LOGGER.warn("LLM has not called any functions for prompt: {}", prompt);
		} catch (Exception e) {
			Thread.currentThread().interrupt();
			LOGGER.error("Could not generate response / execute functions for prompt: {}", prompt, e);
		}
		return StringUtils.EMPTY;
	}

	private String formatChatHistory(List<OllamaChatMessage> history) {
		StringBuilder formattedHistory = new StringBuilder();
		history.stream()
				.filter(msg -> msg.getRole().equals(OllamaChatMessageRole.ASSISTANT))
				.flatMap(msg -> msg.getToolCalls().stream())
				.map(OllamaChatToolCalls::getFunction)
				.forEach(function -> appendFunctionDetails(formattedHistory, function));
		return formattedHistory.toString();
	}

	private void appendFunctionDetails(StringBuilder builder, OllamaToolCallsFunction function) {
		builder.append(function.getName())
				.append(" - args: ")
				.append(function.getArguments())
				.append(StringUtils.SPACE);
	}

	@Override
	public double[] generateEmbedding(List<String> prompt) {
		return CompletableFuture.supplyAsync(() -> {
				try {
					OllamaEmbedResponseModel responseModel = ollamaAPI.embed(OllamaModelType.NOMIC_EMBED_TEXT, prompt);
					return convertEmbedding(responseModel.getEmbeddings());
				} catch (Exception e) {
					Thread.currentThread().interrupt();
					throw new CompletionException(
							"Error generating embedding for prompt: " + prompt, e);
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
