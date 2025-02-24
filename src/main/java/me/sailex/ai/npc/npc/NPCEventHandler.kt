package me.sailex.ai.npc.npc

import me.sailex.ai.npc.history.ConversationHistory
import me.sailex.ai.npc.llm.IFunctionCaller
import me.sailex.ai.npc.llm.function_calling.IFunctionManager
import me.sailex.ai.npc.util.LogUtil
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class NPCEventHandler<T>(
    val llmClient: IFunctionCaller<T>,
    val history: ConversationHistory,
    val functionManager: IFunctionManager<T>
) {
    private val executorService: ExecutorService = Executors.newSingleThreadExecutor()

    /**
     * Processes an event asynchronously by allowing call actions from the llm using the specified prompt.
     * Saves the prompt in conversation history.
     *
     * @param source  source of the prompt
     * @param prompt  prompt of a user or system e.g. chatmessage of a player
     */
    fun onEvent(source: String, prompt: String) {
        CompletableFuture.runAsync({
            history.add(prompt)
            val relevantFunctions = functionManager.getRelevantFunctions(prompt)
            history.add(llmClient.callFunctions(source, prompt, relevantFunctions))
        }, executorService)
            .exceptionally {
                LogUtil.error("Unexpected error occurred handling event: $it", true)
                null
            }
    }

}