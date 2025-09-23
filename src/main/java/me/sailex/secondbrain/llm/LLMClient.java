package me.sailex.secondbrain.llm;

import me.sailex.secondbrain.history.Message;

import java.util.List;

public interface LLMClient {

    /**
     * Lets the LLM generate a chat response to the conversationHistory.
     * @param messages conversationHistory
     * @return responseMessage
     */
    Message chat(List<Message> messages);

    /**
     * Check if the service is reachable
     */
    void checkServiceIsReachable();

	/**
	 * Stops the Executor service
	 */
	void stopService();
}
