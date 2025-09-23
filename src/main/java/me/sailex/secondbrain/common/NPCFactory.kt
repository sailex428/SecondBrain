package me.sailex.secondbrain.common

import com.mojang.authlib.GameProfile
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
import me.sailex.secondbrain.llm.LLMType
import me.sailex.secondbrain.llm.ollama.OllamaClient
import me.sailex.secondbrain.llm.ollama.function_calling.OllamaFunctionManager
import me.sailex.secondbrain.llm.player2.Player2APIClient
import me.sailex.secondbrain.llm.player2.function_calling.Player2FunctionManager
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

    private var executorService: ExecutorService = Executors.newSingleThreadExecutor()
    val uuidToNpc = ConcurrentHashMap<UUID, NPC>()

    fun createNpc(config: NPCConfig, server: MinecraftServer, spawnPos: BlockPos?, owner: PlayerEntity?) {
        CompletableFuture.runAsync({
            val name = config.npcName
            checkLimit()
            checkNpcName(name)

            NPCSpawner.spawn(GameProfile(config.uuid, name), server, spawnPos) { npcEntity ->
                config.uuid = npcEntity.uuid
                val npc = createNpcInstance(npcEntity, config)
                npc.controller.owner = owner

                val matchingConfig = configProvider.getNpcConfig(config.uuid)
                if (matchingConfig.isEmpty) {
                    configProvider.addNpcConfig(config)
                } else {
                    matchingConfig.get().isActive = true
                }
                uuidToNpc[config.uuid] = npc

                LogUtil.infoInChat(("Added NPC with name: $name"))

                npc.eventHandler.onEvent(Instructions.INITIAL_PROMPT)
            }

            NPCEvents.ON_DEATH.register {
                removeNpc(it.uuid, it.server.playerManager)
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
            NPCSpawner.remove(npcToRemove.entity.uuid, playerManager)
            npcToRemove.llmClient.stopService()
            npcToRemove.eventHandler.stopService()
            npcToRemove.contextProvider.chunkManager.stopService()
            resourceProvider.addConversations(uuid,npcToRemove.history.latestConversations)
            uuidToNpc.remove(uuid)

            val config = configProvider.getNpcConfig(uuid).get()
            config.isActive = false
            LogUtil.infoInChat("Removed NPC with name ${config.npcName}")
        } else {
            LogUtil.errorInChat("No NPC with the uuid: $uuid exists!")
        }
    }

    fun deleteNpc(uuid: UUID, playerManager: PlayerManager) {
        removeNpc(uuid, playerManager)
        configProvider.deleteNpcConfig(uuid)
    }

    fun shutdownNPCs(server: MinecraftServer) {
        uuidToNpc.keys.forEach {
            removeNpc(it, server.playerManager)
        }
        executorService.shutdownNow()
    }

    private fun createNpcInstance(npcEntity: ServerPlayerEntity, config: NPCConfig): NPC {
        val baseConfig = configProvider.baseConfig
        val contextProvider = ContextProvider(npcEntity, baseConfig)

        val controller = initController(npcEntity)
        val defaultPrompt = Instructions.getLlmSystemPrompt(config.npcName, config.llmCharacter, controller.commandExecutor.allCommands())

        return when (config.llmType) {
            LLMType.OLLAMA -> {
                val llmClient = OllamaClient(config.ollamaUrl, baseConfig.llmTimeout, baseConfig.isVerbose)
                val history = ConversationHistory(llmClient, defaultPrompt)
                val eventHandler = NPCEventHandler(llmClient, history, contextProvider, controller, config)
                NPC(npcEntity, llmClient, history, eventHandler, controller, contextProvider, config)
            }
            LLMType.PLAYER2 -> {
                val llmClient = Player2APIClient(config.voiceId, config.npcName, baseConfig.llmTimeout)
                val history = ConversationHistory(llmClient, defaultPrompt)
                val eventHandler = NPCEventHandler(llmClient, history, contextProvider, controller, config)
                NPC(npcEntity, llmClient, history, eventHandler, controller, contextProvider, config)
            }
            else -> throw NPCCreationException("Invalid LLM type: ${config.llmType}")
        }
    }

    private fun initController(npcEntity: ServerPlayerEntity): AltoClefController {
        val automatone = BaritoneAPI.getProvider().getBaritone(npcEntity)
        return AltoClefController(automatone)
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

}
