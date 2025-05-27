package me.sailex.secondbrain.llm;

import java.util.List;

public interface LLMClient {

	/**
	 * Generate an embedding for a given prompt
	 *
	 * @param prompt the text prompt that needs to get vectorized
	 * @return the embedding as double array
	 */
	double[] generateEmbedding(List<String> prompt);

	/**
	 * Check if the service is reachable
	 */
	void checkServiceIsReachable();

	/**
	 * Stops the Executor service
	 */
	void stopService();
}
