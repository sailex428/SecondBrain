package me.sailex.secondbrain.event

import com.google.gson.GsonBuilder
import me.sailex.altoclef.AltoClefController
import me.sailex.altoclef.tasks.LookAtOwnerTask
import me.sailex.secondbrain.config.NPCConfig
import me.sailex.secondbrain.constant.Instructions
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
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

class NPCEventHandler(
    private val llmClient: FunctionCallable,
    private val history: ConversationHistory,
    private val contextProvider: ContextProvider,
    private val controller: AltoClefController,
    private val config: NPCConfig,
): EventHandler {
    companion object {
        private val gson = GsonBuilder()
            .setLenient()
            .create()
    }

    private val executorService: ThreadPoolExecutor = ThreadPoolExecutor(
        1, 1, 0L, TimeUnit.MILLISECONDS,
        ArrayBlockingQueue(10),
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

            history.add(Message(formattedPrompt, role.toString().lowercase()))
            val response = llmClient.callFunctions(history.latestConversations)
            history.add(response)

            val parsedMessage = parse(response.message)
            execute(parsedMessage.command)

            if (llmClient is Player2APIClient && config.isTTS) {
                llmClient.startTextToSpeech(parsedMessage.message)
            } else {
                controller.controllerExtras.chat(parsedMessage.message)
            }
        }, executorService)
            .exceptionally {
                LogUtil.debugInChat("Error occurred while handling prompt: ${it.cause?.message}")
                LogUtil.error("Error occurred handling event: $prompt", it.cause)
                null
            }
    }

    override fun onEvent(prompt: String) {
        this.onEvent(ChatRole.USER, prompt)
    }

    override fun stopService() {
        executorService.shutdown()
    }

    override fun queueIsEmpty(): Boolean {
        return executorService.queue.isEmpty()
    }

    //TODO: refactor this
    fun parse(content: String): CommandMessage {
        val message = gson.fromJson(content, CommandMessage::class.java)
        return message
    }

    fun execute(command: String) {
        val cmdExecutor = controller.commandExecutor
        val commandWithPrefix = if (cmdExecutor.isClientCommand(command)) {
            command
        } else {
            cmdExecutor.commandPrefix + command
        }
        cmdExecutor.execute(commandWithPrefix, {
            controller.runUserTask(LookAtOwnerTask())
            if (queueIsEmpty()) {
                //this.onEvent(Instructions.COMMAND_FINISHED_PROMPT.format(commandWithPrefix))
            }
        }, {
            this.onEvent(Instructions.COMMAND_ERROR_PROMPT.format(commandWithPrefix, it.message))
            LogUtil.error("Error executing command: $commandWithPrefix", it)
        })
    }

    data class CommandMessage(
        val command: String,
        val message: String
    )

}