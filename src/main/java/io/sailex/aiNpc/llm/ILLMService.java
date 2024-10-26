package io.sailex.aiNpc.llm;

import java.util.concurrent.CompletableFuture;

public interface ILLMService {

	CompletableFuture<String> generateResponse(String prompt);

	void stopService();
}
