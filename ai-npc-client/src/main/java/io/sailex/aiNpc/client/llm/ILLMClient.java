package io.sailex.aiNpc.client.llm;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Interface for the LLM client
 */
public interface ILLMClient {

	/**
	 * Generate a response to a user and system prompt
	 *
	 * @param userPrompt the prompt from the user
	 * @param systemPrompt the prompt from the system/game
	 * @return a CompletableFuture with the llm response
	 */
	CompletableFuture<String> generateResponse(String userPrompt, String systemPrompt);

	/**
     * Generate an embedding for a given prompt
     *
     * @param prompt the text prompt
     * @return the embedding as float array
     */
	Float[] generateEmbedding(List<String> prompt);

	/**
	 * Stop the execution of the service
	 */
	void stopService();
}
