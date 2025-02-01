package me.sailex.ai.npc.llm;

import me.sailex.ai.npc.history.ConversationHistory;
import me.sailex.ai.npc.llm.function_calling.IFunctionManager;

import java.util.List;

public interface ILLMClient {

	/**
	 * Executes functions that are called by openai based on the prompt
	 *
	 * @param source the source of the prompt e.g. system
	 * @param prompt the prompt
	 */
	void callFunctions(String source, String prompt, ConversationHistory history);

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

	void setFunctionManager(IFunctionManager functionManager);

	void stopService();
}
