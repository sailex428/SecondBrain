package me.sailex.secondbrain.common

import me.sailex.altoclef.AltoClefController
import me.sailex.automatone.api.BaritoneAPI
import me.sailex.secondbrain.config.ConfigProvider
import me.sailex.secondbrain.config.NPCConfig
import me.sailex.secondbrain.constant.Instructions
import me.sailex.secondbrain.context.ContextProvider
import me.sailex.secondbrain.event.NPCEventHandler
import me.sailex.secondbrain.exception.NPCCreationException
import me.sailex.secondbrain.history.ConversationHistory
import me.sailex.secondbrain.history.Message
import me.sailex.secondbrain.llm.LLMClient
import me.sailex.secondbrain.llm.LLMType
import me.sailex.secondbrain.llm.ollama.OllamaClient
import me.sailex.secondbrain.llm.openai.OpenAiClient
import me.sailex.secondbrain.llm.player2.Player2APIClient
import me.sailex.secondbrain.model.NPC
import me.sailex.secondbrain.model.database.Conversation
import net.minecraft.server.network.ServerPlayerEntity

class NPCFactory(
    private val configProvider: ConfigProvider,
) {
     fun createNpc(npcEntity: ServerPlayerEntity, config: NPCConfig, loadedConversation: List<Conversation>?): NPC {
        val baseConfig = configProvider.baseConfig
        val contextProvider = ContextProvider(npcEntity, baseConfig)

        val llmClient = initLLMClient(config)

        val controller = initController(npcEntity)
        val defaultPrompt = Instructions.getLlmSystemPrompt(config.npcName,
            config.llmCharacter,
            controller.commandExecutor.allCommands(),
            config.llmType)

        val messages = loadedConversation
            ?.map { Message(it.message, it.role) }
            ?.toMutableList() ?: mutableListOf()
        val history = ConversationHistory(llmClient, defaultPrompt, messages)
        val eventHandler = NPCEventHandler(llmClient, history, contextProvider, controller, config)
        return NPC(npcEntity, llmClient, history, eventHandler, controller, contextProvider, config)
    }

    private fun initController(npcEntity: ServerPlayerEntity): AltoClefController {
        val automatone = BaritoneAPI.getProvider().getBaritone(npcEntity)
        return AltoClefController(automatone)
    }

    private fun initLLMClient(config: NPCConfig): LLMClient {
        val baseConfig = configProvider.baseConfig
        val llmClient = when (config.llmType) {
            LLMType.OLLAMA -> OllamaClient(config.llmModel, config.ollamaUrl, baseConfig.llmTimeout, baseConfig.isVerbose)
            LLMType.OPENAI -> OpenAiClient(config.llmModel, config.openaiApiKey, baseConfig.llmTimeout)
            LLMType.PLAYER2 -> Player2APIClient(config.voiceId, config.npcName, baseConfig.llmTimeout)
            else -> throw NPCCreationException("Invalid LLM type: ${config.llmType}")
        }
        llmClient.checkServiceIsReachable()
        return llmClient
    }

}
