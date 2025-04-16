package me.sailex.secondbrain.llm;

import io.github.ollama4j.OllamaAPI;
import io.github.ollama4j.models.chat.*;
import io.github.ollama4j.models.embeddings.OllamaEmbedResponseModel;
import io.github.ollama4j.models.request.CustomModelRequest;
import io.github.ollama4j.models.response.Model;
import io.github.ollama4j.tools.OllamaToolCallsFunction;
import io.github.ollama4j.tools.Tools;
import io.github.ollama4j.types.OllamaModelType;
import me.sailex.secondbrain.exception.LLMServiceException;

import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import lombok.Setter;
import me.sailex.secondbrain.model.function_calling.FunctionResponse;
import me.sailex.secondbrain.util.LogUtil;
import org.apache.commons.lang3.StringUtils;

public class OllamaClient extends ALLMClient<Tools.ToolSpecification> implements FunctionCallable<Tools.ToolSpecification> {

	private static final String LLAMA_MODEL_NAME = "llama3.2";
	private static final List<String> REQUIRED_MODELS = List.of(
			OllamaModelType.NOMIC_EMBED_TEXT, LLAMA_MODEL_NAME);

	@Setter
	private OllamaAPI ollamaAPI;
	private final ExecutorService service;
	private final String model;

	public OllamaClient(String url, String customModelName, String defaultPrompt, int timeout) {
		this.ollamaAPI = new OllamaAPI(url);
		this.model = customModelName;
		checkServiceIsReachable(url);
		this.service = Executors.newFixedThreadPool(3);
		ollamaAPI.setVerbose(false);
		ollamaAPI.setMaxChatToolCallRetries(4);
		ollamaAPI.setRequestTimeoutSeconds(timeout);
		initModels(defaultPrompt);
	}

	private void initModels(String defaultPrompt) {
		pullRequiredModels();
		createModelWithPrompt(defaultPrompt);
	}

	private void pullRequiredModels() {
		try {
			Set<String> modelNames = ollamaAPI.listModels().stream()
					.map(Model::getModelName).collect(Collectors.toSet());
			boolean requiredModelsExist = modelNames.containsAll(REQUIRED_MODELS);
			if (!requiredModelsExist) {
				for (String requiredModel : REQUIRED_MODELS) {
					LogUtil.debugInChat("Pulling model: " + requiredModel);
					ollamaAPI.pullModel(requiredModel);
				}
			}
		} catch (Exception e) {
			throw new LLMServiceException("Could not required models: " + REQUIRED_MODELS,  e);
		}
	}

	private void createModelWithPrompt(String defaultPrompt) {
		try {
			LogUtil.debugInChat("Init model: " + model);
			ollamaAPI.createModel(CustomModelRequest.builder()
					.from(LLAMA_MODEL_NAME)
					.model(model)
					.system(defaultPrompt)
					.license("MIT")
					.build());
		} catch (Exception e) {
			throw new LLMServiceException("Could not create model: " + model, e);
		}
	}

	/**
	 * Removes current model.
	 */
	public void removeModel() {
		try {
			LogUtil.debugInChat("Removing model: " + model);
			ollamaAPI.deleteModel(model, false);
		} catch (Exception e) {
			throw new LLMServiceException("Could not remove model: " + model, e);
		}
	}

	/**
	 * Check if the service is reachable.
	 * @throws LLMServiceException if server is not reachable
	 */
	@Override
	public void checkServiceIsReachable(String url) {
		try {
			boolean isOllamaServerReachable = ollamaAPI.ping();
			if (!isOllamaServerReachable) {
				throw new LLMServiceException();
			}
		} catch (Exception e) {
			throw new LLMServiceException("Ollama server is not reachable at: " +  url, e);
		}
	}

	/**
	 * Sends the provided prompt and functions to Ollama API.
	 * Executes functions called by the LLM.
	 *
	 * @param   prompt    the event prompt
	 * @param   functions relevant functions that matches to the prompt
	 * @return  FunctionResponse - the formatted results of the function calls.
	 */
	@Override
	public FunctionResponse callFunctions(String prompt, List<Tools.ToolSpecification> functions) {
		try {
			ollamaAPI.registerTools(functions);

			OllamaChatRequest toolRequest = OllamaChatRequestBuilder.getInstance(model)
				.withMessage(OllamaChatMessageRole.USER, prompt)
				.build();
			OllamaChatResult response = ollamaAPI.chat(toolRequest);

			String finalResponse = response.getResponseModel().getMessage().getContent();
			return new FunctionResponse(finalResponse, formatChatHistory(response.getChatHistory()));
		} catch (Exception e) {
			Thread.currentThread().interrupt();
			LogUtil.error("LLM has not called any functions for prompt: " + prompt);
			return new FunctionResponse("No actions called by LLM.", "");
		}
	}

	private String formatChatHistory(List<OllamaChatMessage> history) {
		StringBuilder formattedHistory = new StringBuilder();
		history.stream()
				.filter(msg -> msg.getRole().equals(OllamaChatMessageRole.TOOL))
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
					throw new LLMServiceException(
							"Error generating embedding for prompt: " + prompt, e);
				}
			}, service)
		.exceptionally(exception -> {
			LogUtil.error(exception.getMessage());
			return new double[] {};
		})
		.join();
	}

	@Override
	public void stopService() {
        try {
            removeModel();
        } catch (Exception e) {
            LogUtil.error("Could not delete model: " + e.getMessage());
        }
		this.service.shutdown();
	}

	public void setVerbose(boolean verbose) {
		this.ollamaAPI.setVerbose(verbose);
	}
}
