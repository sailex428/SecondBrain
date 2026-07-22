package me.sailex.secondbrain.llm.ollama;

import io.github.ollama4j.Ollama;
import io.github.ollama4j.models.chat.*;
import io.github.ollama4j.models.response.Model;
import me.sailex.secondbrain.exception.LLMServiceException;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.Setter;
import me.sailex.secondbrain.history.Message;
import me.sailex.secondbrain.history.MessageConverter;
import me.sailex.secondbrain.llm.LLMClient;
import me.sailex.secondbrain.util.LogUtil;

public class OllamaClient implements LLMClient {

	@Setter
	private Ollama ollama;
	private final String model;
	private final String url;
	private final boolean verbose;

	public OllamaClient(
        String model,
		String url,
		int timeout,
		boolean verbose
	) {
		this.url = url;
		this.ollama = new Ollama(url);
		this.model = model;
		this.verbose = verbose;
		ollama.setRequestTimeoutSeconds(timeout);
		pullRequiredModel(model);
	}

	/**
	 * Check if the service is reachable.
	 * @throws LLMServiceException if server is not reachable
	 */
	@Override
	public void checkServiceIsReachable() {
		try {
			boolean isOllamaServerReachable = ollama.ping();
			if (!isOllamaServerReachable) {
                throw new LLMServiceException("Ollama server is not reachable at: " +  url);
            }
		} catch (Exception e) {
			throw new LLMServiceException("Ollama server is not reachable at: " +  url, e);
		}
	}

	/**
	 * Sends the provided history and functions to Ollama API.
	 * Executes functions called by the LLM.
	 */
	@Override
	public Message chat(List<Message> messages) {
		try {
			OllamaChatRequest request = OllamaChatRequest.builder()
					.withModel(model)
					.withMessages(messages.stream()
							.map(MessageConverter::toOllamaChatMessage)
							.collect(Collectors.toCollection(ArrayList::new)))
					.build();

			OllamaChatResult result = ollama.chat(request, null);
			if (verbose) LogUtil.info("Ollama response: " + result.toString());
			return MessageConverter.toMessage(result.getResponseModel().getMessage());
		} catch (Exception e) {
            throw new LLMServiceException("Could not generate Response for last prompt: " + messages.get(messages.size() - 1).getMessage(), e);
		}
	}

	/**
	 * Pulls the required ollama model if it is not already present.
	 */
    private void pullRequiredModel(String model) {
        try {
            Set<String> models = ollama.listModels().stream()
                    .map(Model::getModelName).collect(Collectors.toSet());
            boolean requiredModelsExist = models.contains(model);
            if (!requiredModelsExist) {
				LogUtil.debugInChat("Pulling model: " + model);
				ollama.pullModel(model);
            }
        } catch (Exception e) {
            throw new LLMServiceException("Could not pull required model: " + model,  e);
        }
    }

	@Override
	public void stopService() {}
}
