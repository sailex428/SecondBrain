package me.sailex.secondbrain.event

import me.sailex.altoclef.control.PlayerExtraController
import me.sailex.secondbrain.config.NPCConfig
import me.sailex.secondbrain.context.ContextProvider
import me.sailex.secondbrain.history.ConversationHistory
import me.sailex.secondbrain.history.Message
import me.sailex.secondbrain.llm.FunctionCallable
import me.sailex.secondbrain.llm.player2.Player2APIClient
import me.sailex.secondbrain.llm.roles.ChatRole
import me.sailex.secondbrain.util.LogUtil
import me.sailex.secondbrain.util.PromptFormatter
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

class NPCEventHandler(
    private val llmClient: FunctionCallable,
    private val history: ConversationHistory,
    private val contextProvider: ContextProvider,
    private val extraController: PlayerExtraController,
    private val config: NPCConfig,
): EventHandler {

    private val executorService: ExecutorService = ThreadPoolExecutor(
        1, 1, 0L, TimeUnit.MILLISECONDS,
        ArrayBlockingQueue<Runnable>(10),
        ThreadPoolExecutor.DiscardPolicy()
    )

    /**
     * Processes an event asynchronously by allowing call actions from llm using the specified prompt.
     * Saves the prompt and responses in conversation history.
     *
     * @param prompt prompt of a user or system e.g. chatmessage of a player
     */
    override fun onEvent(role: ChatRole, prompt: String) {
        CompletableFuture.runAsync({
            LogUtil.info("onEvent: $prompt")

            var formattedPrompt: String
            if (role == ChatRole.USER) {
                formattedPrompt = PromptFormatter.format(prompt,contextProvider.buildContext())
            } else {
                formattedPrompt = "system prompt: $prompt"
            }

            history.add(Message(role.toString(), formattedPrompt))
            val response = llmClient.callFunctions(history.latestConversations)
            history.add(response)

            if (llmClient is Player2APIClient && config.isTTS) {
                llmClient.startTextToSpeech(response.message)
            } else {
                extraController.chat(response.message)
            }
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