package me.sailex.secondbrain.llm.ollama;

import io.github.ollama4j.OllamaAPI;
import io.github.ollama4j.models.chat.*;
import io.github.ollama4j.models.embeddings.OllamaEmbedResponseModel;
import io.github.ollama4j.models.request.CustomModelRequest;
import io.github.ollama4j.models.response.Model;
import io.github.ollama4j.tools.Tools;
import io.github.ollama4j.types.OllamaModelType;
import me.sailex.secondbrain.exception.LLMServiceException;

import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import lombok.Setter;
import me.sailex.secondbrain.llm.ALLMClient;
import me.sailex.secondbrain.llm.roles.BasicRole;
import me.sailex.secondbrain.util.LogUtil;

public class OllamaClient extends ALLMClient<Tools.ToolSpecification> {

	private static final String LLAMA_MODEL_NAME = "llama3.2";
	private static final List<String> REQUIRED_MODELS = List.of(
			OllamaModelType.NOMIC_EMBED_TEXT, LLAMA_MODEL_NAME);

	@Setter
	private OllamaAPI ollamaAPI;
	private final ExecutorService service;
	private final String model;
	private final String url;

	public OllamaClient(
		String url,
		String customModelName,
		String defaultPrompt,
		int timeout,
		boolean verbose
	) {
		this.url = url;
		this.ollamaAPI = new OllamaAPI(url);
		this.model = customModelName;
		checkServiceIsReachable();
		this.service = Executors.newFixedThreadPool(3);
		ollamaAPI.setVerbose(verbose);
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
			ollamaAPI.deleteModel(model, true);
		} catch (Exception e) {
			Thread.currentThread().interrupt();
			throw new LLMServiceException("Could not remove model: " + model, e);
		}
	}

	/**
	 * Check if the service is reachable.
	 * @throws LLMServiceException if server is not reachable
	 */
	@Override
	public void checkServiceIsReachable() {
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
	 * @param   functions relevant functions that match to the prompt
	 * @return  response - the formatted results of the function calls.
	 */
	@Override
	public String callFunctions(
		BasicRole role,
		String prompt,
		List<Tools.ToolSpecification> functions
	) throws LLMServiceException {
		try {
			ollamaAPI.registerTools(functions);
			OllamaChatRequest toolRequest = OllamaChatRequestBuilder.getInstance(model)
				.withMessage(OllamaChatMessageRole.getRole(role.getRoleName()), prompt)
				.build();
			OllamaChatResult response = ollamaAPI.chat(toolRequest);

			return response.getResponseModel().getMessage().getContent();
		} catch (Exception e) {
			Thread.currentThread().interrupt();
			throw new LLMServiceException("Could not call functions for prompt: " + prompt, e);
		}
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
}
