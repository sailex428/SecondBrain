package me.sailex.secondbrain.llm;

import me.sailex.secondbrain.history.Message;

public interface LLMClient {

	/**
	 * Check if the service is reachable
	 */
	void checkServiceIsReachable();

    Message chat(Message messages);

	/**
	 * Stops the Executor service
	 */
	void stopService();
}
