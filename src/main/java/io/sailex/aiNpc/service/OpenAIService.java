package io.sailex.aiNpc.service;

import java.util.concurrent.CompletableFuture;

public class OpenAIService implements ILLMService {

	private final String apiKey;

	public OpenAIService(String apiKey) {
		this.apiKey = apiKey;
	}

	@Override
	public CompletableFuture<String> generateResponse(String message) {
		return null;
	}
}
