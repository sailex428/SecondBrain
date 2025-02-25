package me.sailex.ai.npc.llm;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.github.ollama4j.OllamaAPI;
import io.github.ollama4j.exceptions.ToolInvocationException;
import io.github.ollama4j.tools.OllamaToolsResult;
import io.github.ollama4j.tools.Tools;
import io.github.ollama4j.types.OllamaModelType;
import io.github.ollama4j.utils.OptionsBuilder;
import me.sailex.ai.npc.constant.Instructions;
import me.sailex.ai.npc.exception.LLMServiceException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import lombok.Setter;
import me.sailex.ai.npc.util.LogUtil;
import org.apache.commons.lang3.StringUtils;

/**
 * Ollama client for generating responses.
 */
public class OllamaClient extends ALLMClient<Tools.ToolSpecification> {

	@Setter
	private OllamaAPI ollamaAPI;
	private final ExecutorService service;
	private final String model;

	/**
	 * Constructor for OllamaClient.
	 *
	 * @param model the ollama model (e.g. "gemma2")
	 * @param url   the ollama url
	 */
	public OllamaClient(String model, String url) {
		this.ollamaAPI = new OllamaAPI(url);
		ollamaAPI.setVerbose(true);
		ollamaAPI.setRequestTimeoutSeconds(30);
		this.model = model;
		this.service = Executors.newFixedThreadPool(3);
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
	 * Executes function calls on the provided source or prompt using a LLM. Each function call is executed in sequence,
	 * with each result being appended to the current context for conversation history.
	 * The process continues until either, no functions are left in the list or the LLM stops calling any additional tools
	 *
	 * @param   source the source of the prompt e.g. system
	 * @param   prompt the chat/system prompt
	 * @param   functions relevant functions that matches to the prompt
	 * @return  String of called functions with their arguments
	 */
	@Override
	public String callFunctions(String source, String prompt, List<Tools.ToolSpecification> functions) {
		try {
			StringBuilder calledFunctions = new StringBuilder();
			StringBuilder currentPrompt = new StringBuilder(prompt);

			//execute functions until llm doesnt call anyOfThem anymore, limit to 4 iteration, maybe llm do stupid things
			for (int i = 0; i < functions.size(); i++) {
				OllamaToolsResult toolsResult = ollamaAPI.generateWithTools(
						this.model,
						buildPrompt(currentPrompt, functions),
						new OptionsBuilder().setTemperature(0.3f).build()
				);

				List<OllamaToolsResult.ToolResult> toolResults = toolsResult.getToolResults();
				if (toolResults == null || toolResults.isEmpty()) {
					break;
				}
				toolsResult.getToolResults().forEach(toolResult -> {
					currentPrompt.append(" - ").append(toolResult.getResult().toString());
					//needed for convo history
					calledFunctions.append(toolResult.getFunctionName())
							.append(" - args: ")
							.append(toolResult.getFunctionArguments())
							.append(StringUtils.SPACE);
					removeCalledFunctions(functions, toolResult.getFunctionName());
				});
			}
			return calledFunctions.toString();
		} catch (JacksonException e) {
			LOGGER.warn("LLM has not called any functions for prompt: {}", prompt);
		} catch (ToolInvocationException e) {
			LOGGER.warn("LLM seems to be hallucinating: {}", e.getMessage());
		} catch (Exception e) {
			Thread.currentThread().interrupt();
			LOGGER.error("Could not generate response / execute functions for prompt: {}", prompt, e);
		}
		return StringUtils.EMPTY;
	}

	private void removeCalledFunctions(List<Tools.ToolSpecification> functions, String functionName) {
		List<Tools.ToolSpecification> functionsToRemove = new ArrayList<>();
		functions.forEach(func -> {
			if (func.getFunctionName().equals(functionName)) {
				functionsToRemove.add(func);
			}
		});
		functions.removeAll(functionsToRemove);
	}

	private String buildPrompt(StringBuilder currentPrompt, List<Tools.ToolSpecification> functions) throws JsonProcessingException {
		Tools.PromptBuilder promptBuilder = new Tools.PromptBuilder();
		functions.forEach(promptBuilder::withToolSpecification);
		promptBuilder.withPrompt(Instructions.PROMPT_PREFIX + currentPrompt.toString());
		return promptBuilder.build();
	}

	@Override
	public double[] generateEmbedding(List<String> prompt) {
		return CompletableFuture.supplyAsync(() -> {
				try {
					return convertEmbedding(Collections.singletonList(ollamaAPI
							.generateEmbeddings(OllamaModelType.NOMIC_EMBED_TEXT, prompt.getFirst())));
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
