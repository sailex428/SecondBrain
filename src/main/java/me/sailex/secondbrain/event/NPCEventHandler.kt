package me.sailex.secondbrain.event

import me.sailex.secondbrain.common.NPCController
import me.sailex.secondbrain.config.NPCConfig
import me.sailex.secondbrain.context.ContextProvider
import me.sailex.secondbrain.history.ConversationHistory
import me.sailex.secondbrain.llm.FunctionCallable
import me.sailex.secondbrain.llm.function_calling.FunctionManager
import me.sailex.secondbrain.llm.player2.Player2APIClient
import me.sailex.secondbrain.llm.roles.ChatRole
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
    private val controller: NPCController,
    private val config: NPCConfig
): EventHandler {
    private val executorService: ExecutorService = Executors.newSingleThreadExecutor()

    /**
     * Processes an event asynchronously by allowing call actions from llm using the specified prompt.
     * Saves the prompt and responses in conversation history.
     *
     * @param prompt prompt of a user or system e.g. chatmessage of a player
     */
    override fun onEvent(role: ChatRole, prompt: String) {
        CompletableFuture.runAsync({
            LogUtil.info("onEvent: $prompt")

            var formattedPrompt = prompt
            if (role == ChatRole.USER) {
                formattedPrompt = PromptFormatter.format(
                    prompt,
                    contextProvider.buildContext(),
                    history.getConversations()
                )
            }
            val relevantFunctions = functionManager.getRelevantFunctions(prompt)
            val response = llmClient.callFunctions(role, formattedPrompt, relevantFunctions)

            if (llmClient is Player2APIClient && config.isTTS) {
                llmClient.startTextToSpeech(response)
            } else {
                controller.addGoal("chat") { controller.chat(response) }
            }
            history.add(role, prompt)
            history.add(ChatRole.ASSISTANT, response )
        }, executorService)
            .exceptionally {
                LogUtil.debugInChat("'" + config.npcName + "' didn’t understand what to do. The AI response may have failed.")
                LogUtil.error("Error occurred handling event", it.cause)
                null
            }
    }

    override fun onEvent(prompt: String) {
        this.onEvent(ChatRole.USER, prompt)
    }

    override fun stopService() {
        executorService.shutdown()
    }

}