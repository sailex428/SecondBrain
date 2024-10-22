package io.sailex.aiNpc.service;

import java.util.concurrent.CompletableFuture;

public interface ILLMService {

	CompletableFuture<String> generateResponse(String prompt);
}
