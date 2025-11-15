package me.sailex.secondbrain.common

import me.sailex.altoclef.AltoClefController
import me.sailex.automatone.api.BaritoneAPI
import me.sailex.secondbrain.callback.NPCEvents
import me.sailex.secondbrain.config.ConfigProvider
import me.sailex.secondbrain.config.NPCConfig
import me.sailex.secondbrain.constant.Instructions
import me.sailex.secondbrain.context.ContextProvider
import me.sailex.secondbrain.database.resources.ResourceProvider
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
import me.sailex.secondbrain.util.LogUtil
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.MinecraftServer
import net.minecraft.server.PlayerManager
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.math.BlockPos
import java.util.UUID
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class NPCFactory(
    private val configProvider: ConfigProvider,
    private val resourceProvider: ResourceProvider
) {
    companion object {
        private const val MAX_NUMBER_OF_NPC = 10
    }

    private lateinit var executorService: ExecutorService
    val uuidToNpc = ConcurrentHashMap<UUID, NPC>()

    fun init() {
        executorService = Executors.newSingleThreadExecutor()
    }

    fun createNpc(newConfig: NPCConfig, server: MinecraftServer, spawnPos: BlockPos?, owner: PlayerEntity?) {
        CompletableFuture.runAsync({
            val name = newConfig.npcName
            checkLimit()
            checkNpcName(name)

            val config = updateConfig(newConfig)
            val llmClient = initLLMClient(config)

            NPCSpawner.spawn(config, server, spawnPos) { npcEntity ->
                config.uuid = npcEntity.uuid
                val npc = createNpcInstance(npcEntity, config, llmClient)
                npc.controller.owner = owner
                uuidToNpc[config.uuid] = npc

                LogUtil.infoInChat(("Added NPC with name: $name"))
                npc.eventHandler.onEvent(Instructions.INITIAL_PROMPT)
            }

            NPCEvents.ON_DEATH.register {
                removeNpc(it.uuid, it.server!!.playerManager)
            }
        }, executorService).exceptionally {
            LogUtil.errorInChat(it.message)
            LogUtil.error(it)
            null
        }
    }

    fun removeNpc(uuid: UUID, playerManager: PlayerManager) {
        val npcToRemove = uuidToNpc[uuid]
        if (npcToRemove != null) {
            npcToRemove.controller.stop()
            npcToRemove.llmClient.stopService()
            npcToRemove.eventHandler.stopService()
            npcToRemove.contextProvider.chunkManager.stopService()
            resourceProvider.addConversations(uuid,npcToRemove.history.latestConversations)
            uuidToNpc.remove(uuid)

            val config = configProvider.getNpcConfig(uuid).get()
            config.isActive = false

            NPCSpawner.remove(npcToRemove.entity.uuid, playerManager)

            LogUtil.infoInChat("Removed NPC with name ${config.npcName}")
        }
    }

    fun deleteNpc(uuid: UUID, playerManager: PlayerManager) {
        resourceProvider.loadedConversations.remove(uuid)
        resourceProvider.conversationRepository.deleteByUuid(uuid)
        removeNpc(uuid, playerManager)
        configProvider.deleteNpcConfig(uuid)
    }

    fun shutdownNPCs(server: MinecraftServer) {
        uuidToNpc.keys.forEach {
            removeNpc(it, server.playerManager)
        }
        executorService.shutdownNow()
    }

    private fun createNpcInstance(npcEntity: ServerPlayerEntity, config: NPCConfig, llmClient: LLMClient): NPC {
        val baseConfig = configProvider.baseConfig
        val contextProvider = ContextProvider(npcEntity, baseConfig)

        val controller = initController(npcEntity)
        val defaultPrompt = Instructions.getLlmSystemPrompt(config.npcName, config.llmCharacter, controller.commandExecutor.allCommands())

        val messages = resourceProvider.loadedConversations[npcEntity.uuid]
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

    private fun checkNpcName(npcName: String) {
        if (uuidToNpc.values.any { it.entity.name.string == npcName }) {
            throw NPCCreationException("A NPC with the name '$npcName' already exists.")
        }
    }

    private fun checkLimit() {
        if (uuidToNpc.size == MAX_NUMBER_OF_NPC) {
            throw NPCCreationException("Currently there are no more than" + MAX_NUMBER_OF_NPC +" parallel running " +
                    "NPCs supported!")
        }
    }

    private fun updateConfig(newConfig: NPCConfig): NPCConfig {
        val config = configProvider.getNpcConfigByName(newConfig.npcName)
        if (config.isEmpty) {
            return configProvider.addNpcConfig(newConfig)
        } else {
            config.get().isActive = true
            return config.get()
        }
    }

}
