package me.sailex.secondbrain.event

import me.sailex.secondbrain.common.NPCController
import me.sailex.secondbrain.context.ContextProvider
import me.sailex.secondbrain.history.ConversationHistory
import me.sailex.secondbrain.llm.FunctionCallable
import me.sailex.secondbrain.llm.OllamaClient
import me.sailex.secondbrain.llm.function_calling.FunctionManager
import me.sailex.secondbrain.util.LogUtil
import me.sailex.secondbrain.util.PromptFormatter
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class NPCEventHandler<T>(
    private val llmClient: FunctionCallable<T>,
    private val history: ConversationHistory,
    private val functionManager: FunctionManager<T>,
    private val contextProvider: ContextProvider,
    private val controller: NPCController
): EventHandler {
    private val executorService: ExecutorService = Executors.newSingleThreadExecutor()

    /**
     * Processes an event asynchronously by allowing call actions from llm using the specified prompt.
     * Saves the prompt and responses in conversation history.
     *
     * @param prompt prompt of a user or system e.g. chatmessage of a player
     */
    override fun onEvent(prompt: String) {
        CompletableFuture.runAsync({
            LogUtil.info("onEvent: $prompt")
            history.add(prompt)

            val relevantFunctions = functionManager.getRelevantFunctions(prompt)
            val context = contextProvider.buildContext()
            val formattedPrompt = PromptFormatter.format(prompt, context)

            val response = llmClient.callFunctions(formattedPrompt, relevantFunctions)

            if (llmClient is OllamaClient) {
                controller.addGoal("chat") { controller.chat(response.finalResponse) }
                history.add(response.finalResponse + " - " + response.toolCalls)
            } else {
                history.add(response.toolCalls)
            }
        }, executorService)
            .exceptionally {
                LogUtil.error("Unexpected error occurred handling event", it)
                null
            }
    }

    override fun stopService() {
        executorService.shutdown()
    }

}