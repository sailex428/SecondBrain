package me.sailex.secondbrain.common

import baritone.api.BaritoneAPI
import me.sailex.secondbrain.SecondBrain
import me.sailex.secondbrain.config.ConfigProvider
import me.sailex.secondbrain.config.NPCConfig
import me.sailex.secondbrain.constant.Instructions
import me.sailex.secondbrain.context.ContextProvider
import me.sailex.secondbrain.database.repositories.RepositoryFactory
import me.sailex.secondbrain.database.resources.ResourcesProvider
import me.sailex.secondbrain.event.EventHandler
import me.sailex.secondbrain.event.NPCEventHandler
import me.sailex.secondbrain.exception.NPCCreationException
import me.sailex.secondbrain.history.ConversationHistory
import me.sailex.secondbrain.llm.LLMClient
import me.sailex.secondbrain.llm.LLMType
import me.sailex.secondbrain.llm.OllamaClient
import me.sailex.secondbrain.llm.OpenAiClient
import me.sailex.secondbrain.llm.function_calling.OllamaFunctionManager
import me.sailex.secondbrain.llm.function_calling.OpenAiFunctionManager
import me.sailex.secondbrain.mode.ModeController
import me.sailex.secondbrain.mode.ModeInitializer
import me.sailex.secondbrain.model.NPC
import me.sailex.secondbrain.util.LogUtil
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.PlayerManager
import net.minecraft.server.network.ServerPlayerEntity
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class NPCFactory(
    private val configProvider: ConfigProvider,
    private val repositoryFactory: RepositoryFactory
) {
    val npcSpawner = NPCSpawner()
    val nameToNpc = mutableMapOf<String, NPC>()
    var resourcesProvider: ResourcesProvider? = null
        private set

    fun createNpc(config: NPCConfig, source: PlayerEntity) {
        checkLimit()
        var name = config.npcName
        checkNpcName(name)

        npcSpawner.spawn(source, name)
        val latch = CountDownLatch(1)
        npcSpawner.checkPlayerAvailable(name, latch)
        Thread {
            try {
                //player spawning is nonblocking so we need to wait here until its avail
                val npcWasCreated = latch.await(3, TimeUnit.SECONDS)
                val npcEntity = source.server?.playerManager?.getPlayer(name)
                name = npcEntity?.name?.string //FIXME: npcSpawner should create npc with same casing as typed in
                if (!npcWasCreated || npcEntity == null) {
                    throw NPCCreationException("NPCEntity with name: $name could not be spawned.")
                }
                val npc = createNpcInstance(npcEntity, config)
                nameToNpc[name] = npc
                handleInitMessage(npc.eventHandler)
            } catch (e: Exception) {
                Thread.currentThread().interrupt()
                throw NPCCreationException("Failed to create NPC: " + e.message)
            }
        }.start()

        val matchingConfig = configProvider.getNpcConfig(name)
        if (matchingConfig.isEmpty) {
            configProvider.addNpcConfig(config)
        } else {
            matchingConfig.get().isActive = true
        }
        LogUtil.info(("Added NPC with name: ${config.npcName}"))
    }

    fun removeNpc(name: String, playerManager: PlayerManager) {
        val npcToRemove = nameToNpc[name]
        if (npcToRemove != null) {
            configProvider.getNpcConfig(name).ifPresent { it.isActive = false }
            npcSpawner.despawn(name, playerManager)
            npcToRemove.llmClient.stopService()
            npcToRemove.eventHandler.stopService()
            npcToRemove.modeController.setAllIsOn(false)
            npcToRemove.npcController.cancelActions()
            nameToNpc.remove(name)
            LogUtil.info("Removed NPC with name: $name")
        }
    }

    fun deleteNpc(name: String, playerManager: PlayerManager) {
        removeNpc(name, playerManager)
        configProvider.deleteNpcConfig(name)
    }

    fun shutdownNpcs() {
        nameToNpc.values.forEach {
            it.eventHandler.stopService()
            it.llmClient.stopService()
        }
    }

    private fun createNpcInstance(npcEntity: ServerPlayerEntity, config: NPCConfig): NPC {
        val baseConfig = configProvider.baseConfig
        val contextProvider = ContextProvider(npcEntity, baseConfig)

        return when (config.llmType) {
            LLMType.OLLAMA -> {
                val llmClient = OllamaClient(config.ollamaUrl, SecondBrain.MOD_ID + "-" + config.npcName, config.llmDefaultPrompt, baseConfig.llmTimeout)
                val (controller, history) = initBase(llmClient, npcEntity, config.npcName, contextProvider)
                val functionManager = OllamaFunctionManager(
                    resourcesProvider!!,
                    controller,
                    contextProvider,
                    llmClient
                )
                val eventHandler = NPCEventHandler(llmClient, history, functionManager, contextProvider, controller)
                val modeController = initModeController(npcEntity, controller, contextProvider)

                NPC(npcEntity, llmClient, history, eventHandler, controller, contextProvider, modeController, config)
            }
            LLMType.OPENAI -> {
                val llmClient = OpenAiClient(config.openaiApiKey, baseConfig.llmTimeout)
                val (controller, history) = initBase(llmClient, npcEntity, config.npcName, contextProvider)
                val functionManager = OpenAiFunctionManager(
                    resourcesProvider!!,
                    controller,
                    contextProvider,
                    llmClient
                )
                val eventHandler = NPCEventHandler(llmClient, history, functionManager, contextProvider, controller)
                val modeController = initModeController(npcEntity, controller, contextProvider)

                NPC(npcEntity, llmClient, history, eventHandler, controller, contextProvider, modeController, config)
            }
            else -> throw NPCCreationException("Invalid LLM type: ${config.llmType}")
        }
    }

    private fun initBase(
        llmClient: LLMClient,
        npcEntity: ServerPlayerEntity,
        npcName: String,
        contextProvider: ContextProvider
    ): Pair<NPCController, ConversationHistory> {
        initResourceProvider(llmClient, npcName)
        val automatone = BaritoneAPI.getProvider().getBaritone(npcEntity)
        val controller = NPCController(npcEntity, automatone, contextProvider)
        val history = ConversationHistory(resourcesProvider!!, npcName)
        return Pair(controller, history)
    }

    private fun initResourceProvider(llmClient: LLMClient, npcName: String) {
        this.resourcesProvider ?: run {
            this.resourcesProvider = ResourcesProvider(
                repositoryFactory.conversationRepository,
                repositoryFactory.recipesRepository,
                llmClient
            )
            resourcesProvider?.loadResources(npcName)
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

    private fun handleInitMessage(eventHandler: EventHandler) {
        eventHandler.onEvent(Instructions.INIT_PROMPT)
    }

    fun checkNpcName(npcName: String) {
        if (nameToNpc.containsKey(npcName)) {
            throw NPCCreationException("A NPC with the name '$npcName' already exists.")
        }
    }

    fun checkLimit() {
        if (nameToNpc.size == 3) {
            throw NPCCreationException("Currently there are no more than 3 parallel running NPCs supported!")
        }
    }

}
