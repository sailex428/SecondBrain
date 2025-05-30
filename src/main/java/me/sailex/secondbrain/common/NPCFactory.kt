package me.sailex.secondbrain.common

import baritone.api.BaritoneAPI
import me.sailex.secondbrain.SecondBrain
import me.sailex.secondbrain.config.ConfigProvider
import me.sailex.secondbrain.config.NPCConfig
import me.sailex.secondbrain.constant.Instructions
import me.sailex.secondbrain.context.ContextProvider
import me.sailex.secondbrain.database.repositories.RepositoryFactory
import me.sailex.secondbrain.database.resources.ResourcesProvider
import me.sailex.secondbrain.event.NPCEventHandler
import me.sailex.secondbrain.exception.NPCCreationException
import me.sailex.secondbrain.history.ConversationHistory
import me.sailex.secondbrain.llm.LLMClient
import me.sailex.secondbrain.llm.LLMType
import me.sailex.secondbrain.llm.ollama.OllamaClient
import me.sailex.secondbrain.llm.ollama.function_calling.OllamaFunctionManager
import me.sailex.secondbrain.llm.player2.Player2APIClient
import me.sailex.secondbrain.llm.player2.function_calling.Player2FunctionManager
import me.sailex.secondbrain.mode.ModeController
import me.sailex.secondbrain.mode.ModeInitializer
import me.sailex.secondbrain.model.NPC
import me.sailex.secondbrain.util.LogUtil
import net.minecraft.server.MinecraftServer
import net.minecraft.server.PlayerManager
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.math.BlockPos
import java.util.UUID
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

object NPCFactory {

    private const val MAX_NUMBER_OF_NPC = 10
    lateinit var configProvider: ConfigProvider
    private lateinit var repositoryFactory: RepositoryFactory
    private lateinit var executorService: ExecutorService

    fun initialize(configProvider: ConfigProvider, repositoryFactory: RepositoryFactory) {
        this.configProvider = configProvider
        this.repositoryFactory = repositoryFactory
        this.executorService = Executors.newSingleThreadExecutor()
    }

    val uuidToNpc = ConcurrentHashMap<UUID, NPC>()
    var resourcesProvider: ResourcesProvider? = null
        private set

    fun createNpc(config: NPCConfig, server: MinecraftServer, spawnPos: BlockPos?) {
        CompletableFuture.runAsync({
            checkLimit()
            val npcEntity = spawnNpc(config, server, spawnPos)
            val npc = createNpcInstance(npcEntity, config)
            config.uuid = npcEntity.uuid
            val matchingConfig = configProvider.getNpcConfig(config.uuid)
            if (matchingConfig.isEmpty) {
                configProvider.addNpcConfig(config)
            } else {
                matchingConfig.get().isActive = true
            }
            uuidToNpc[npcEntity.uuid] = npc

            LogUtil.infoInChat(("Added NPC with name: ${config.npcName}"))
        }, executorService).exceptionally {
            LogUtil.errorInChat(it.message)
            LogUtil.error(it)
            null
        }
    }

    private fun spawnNpc(config: NPCConfig, server: MinecraftServer, spawnPos: BlockPos?): ServerPlayerEntity {
        val name = config.npcName
        checkNpcName(name)

        val latch = CountDownLatch(1)
        NPCSpawner.spawn(config, server, spawnPos, latch)
        //player spawning runs async so we need to wait here until its avail
        latch.await(3, TimeUnit.SECONDS)
        val npcEntity = server.playerManager?.getPlayer(name)
        if (npcEntity == null) {
            throw NPCCreationException("NPCEntity with name $name could not be spawned. " +
                    "Player profile could get fetched in time!")
        }
        return npcEntity
    }

    fun removeNpc(uuid: UUID, playerManager: PlayerManager) {
        val npcToRemove = uuidToNpc[uuid]
        if (npcToRemove != null) {
            NPCSpawner.remove(npcToRemove.entity.uuid, playerManager)
            stopLlmClient(npcToRemove.llmClient)
            npcToRemove.eventHandler.stopService()
            npcToRemove.modeController.setAllIsOn(false)
            npcToRemove.npcController.cancelActions()
            npcToRemove.contextProvider.chunkManager.stopService()
            uuidToNpc.remove(uuid)
            val config = configProvider.getNpcConfig(uuid).get()
            config.isActive = false
            LogUtil.infoInChat("Removed NPC with name ${config.npcName}")
        }
    }

    private fun stopLlmClient(llmClient: LLMClient) {
        val resourceLlmClient = resourcesProvider?.llmClient
        if (resourceLlmClient != llmClient) {
            llmClient.stopService()
        }
    }

    fun deleteNpc(uuid: UUID, playerManager: PlayerManager) {
        removeNpc(uuid, playerManager)
        configProvider.deleteNpcConfig(uuid)
    }

    fun shutdownNpcs(server: MinecraftServer) {
        uuidToNpc.keys.forEach {
            removeNpc(it, server.playerManager)
        }
        resourcesProvider?.llmClient?.stopService()
        executorService.shutdownNow()
    }

    private fun createNpcInstance(npcEntity: ServerPlayerEntity, config: NPCConfig): NPC {
        val baseConfig = configProvider.baseConfig
        val contextProvider = ContextProvider(npcEntity, baseConfig)

        return when (config.llmType) {
            LLMType.OLLAMA -> {
                val defaultPrompt = Instructions.getLlmSystemPrompt(config.npcName, config.llmCharacter)
                val llmClient = OllamaClient(config.ollamaUrl, SecondBrain.MOD_ID + "-" + config.npcName,
                    defaultPrompt, baseConfig.llmTimeout, baseConfig.isVerbose)

                val (controller, history) = initBase(npcEntity, config.npcName, contextProvider)

                initResourceProvider(llmClient, config.npcName, npcEntity.server)

                val functionManager = OllamaFunctionManager(
                    resourcesProvider!!,
                    controller,
                    contextProvider,
                    llmClient
                )
                val eventHandler = NPCEventHandler(llmClient, history, functionManager,
                    contextProvider, controller, config)
                val modeController = initModeController(npcEntity, controller, contextProvider)
                eventHandler.onEvent(String.format(Instructions.INIT_PROMPT, config.npcName))

                NPC(npcEntity, llmClient, history, eventHandler, controller, contextProvider, modeController, config)
            }
//            LLMType.OPENAI -> {
//                val llmClient = OpenAiClient(config.openaiApiKey, baseConfig.llmTimeout)
//                val (controller, history) = initBase(llmClient, npcEntity, config.npcName, contextProvider)
//                val functionManager = OpenAiFunctionManager(
//                    resourcesProvider!!,
//                    controller,
//                    contextProvider,
//                    llmClient
//                )
//                val eventHandler = NPCEventHandler(llmClient, history, functionManager, contextProvider, controller)
//                val modeController = initModeController(npcEntity, controller, contextProvider)
//
//                NPC(npcEntity, llmClient, history, eventHandler, controller, contextProvider, modeController, config)
//            }
            LLMType.PLAYER2 -> {
                val llmClient = Player2APIClient(config.voiceId, config.npcName, baseConfig.llmTimeout,
                    Instructions.PLAYER2_INIT_PROMPT.format(config.llmCharacter))

                val (controller, history) = initBase(npcEntity, config.npcName, contextProvider)

                val functionManager = Player2FunctionManager(
                    resourcesProvider,
                    controller,
                    contextProvider,
                    llmClient
                )

                val eventHandler = NPCEventHandler(llmClient, history, functionManager,
                    contextProvider, controller, config)
                val modeController = initModeController(npcEntity, controller, contextProvider)

                eventHandler.onEvent(String.format(Instructions.INIT_PROMPT, config.npcName))

                NPC(npcEntity, llmClient, history, eventHandler, controller, contextProvider, modeController, config)
            }
            else -> throw NPCCreationException("Invalid LLM type: ${config.llmType}")
        }
    }

    private fun initBase(
        npcEntity: ServerPlayerEntity,
        npcName: String,
        contextProvider: ContextProvider
    ): Pair<NPCController, ConversationHistory> {
        val automatone = BaritoneAPI.getProvider().getBaritone(npcEntity)
        val controller = NPCController(npcEntity, automatone, contextProvider)
        val history = ConversationHistory(resourcesProvider, npcName)
        return Pair(controller, history)
    }

    private fun initResourceProvider(llmClient: LLMClient, npcName: String, server: MinecraftServer) {
        this.resourcesProvider ?: run {
            this.resourcesProvider = ResourcesProvider(
                repositoryFactory.conversationRepository,
                repositoryFactory.recipesRepository,
                llmClient
            )
            resourcesProvider?.loadResources(npcName, server)
        }
    }

    private fun initModeController(
        npcEntity: ServerPlayerEntity,
        controller: NPCController,
        contextProvider: ContextProvider
    ): ModeController {
        val modes = ModeInitializer(npcEntity, controller, contextProvider).initModes()
        val modeController = ModeController(controller, modes)
        modeController.registerTickListener()
        return modeController
    }

    fun checkNpcName(npcName: String) {
        if (uuidToNpc.values.any { it.entity.name.string == npcName }) {
            throw NPCCreationException("A NPC with the name '$npcName' already exists.")
        }
    }

    fun checkLimit() {
        if (uuidToNpc.size == MAX_NUMBER_OF_NPC) {
            throw NPCCreationException("Currently there are no more than" + MAX_NUMBER_OF_NPC +" parallel running " +
                    "NPCs supported!")
        }
    }

}
