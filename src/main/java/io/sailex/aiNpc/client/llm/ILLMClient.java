package io.sailex.aiNpc.client.llm;

import java.util.concurrent.CompletableFuture;

public interface ILLMClient {

	CompletableFuture<String> generateResponse(String userPrompt, String systemPrompt);

	void stopService();
}
