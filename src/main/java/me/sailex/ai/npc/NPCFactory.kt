package me.sailex.ai.npc

import baritone.api.BaritoneAPI
import me.sailex.ai.npc.config.ModConfig
import me.sailex.ai.npc.constant.ConfigConstants
import me.sailex.ai.npc.constant.Instructions
import me.sailex.ai.npc.database.repositories.RepositoryFactory
import me.sailex.ai.npc.database.resources.ResourcesProvider
import me.sailex.ai.npc.event.IEventHandler
import me.sailex.ai.npc.event.NPCEventHandler
import me.sailex.ai.npc.exception.NPCCreationException
import me.sailex.ai.npc.history.ConversationHistory
import me.sailex.ai.npc.llm.ILLMClient
import me.sailex.ai.npc.llm.LLMType
import me.sailex.ai.npc.llm.OllamaClient
import me.sailex.ai.npc.llm.OpenAiClient
import me.sailex.ai.npc.llm.function_calling.OllamaFunctionManager
import me.sailex.ai.npc.llm.function_calling.OpenAiFunctionManager
import me.sailex.ai.npc.model.NPC
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import org.apache.commons.lang3.StringUtils

class NPCFactory(
    private val config: ModConfig,
    private val repositoryFactory: RepositoryFactory
) {
    val nameToNpc = mutableMapOf<String, NPC>()
    var resourcesProvider: ResourcesProvider? = null
        private set

    fun createNpc(
        server: MinecraftServer,
        npcEntity: ServerPlayerEntity,
        llmType: String,
        llmModel: String
    ) {
        val npcName = npcEntity.name.string
        checkNpcName(npcName)

        val npc = createNpcInstance(server, npcEntity, npcName, llmType, llmModel)

        handleInitMessage(npc.eventHandler, npc.entity.name.string)

        nameToNpc[npcName] = npc
    }

    fun removeNpc(npcName: String): Boolean {
        val npcToRemove = nameToNpc[npcName]
        if (npcToRemove != null) {
            npcToRemove.llmClient.stopService()
            npcToRemove.eventHandler.stopService()
            nameToNpc.remove(npcName)
            return true
        }
        return false
    }

    fun shutdownNpc() {
        nameToNpc.values.forEach {
            it.eventHandler.stopService()
            it.llmClient.stopService()
        }
    }

    private fun checkNpcName(npcName: String) {
        if (nameToNpc.containsKey(npcName)) {
            throw NPCCreationException("An NPC with the name '$npcName' already exists.")
        }
    }

    private fun createNpcInstance(
        server: MinecraftServer,
        npcEntity: ServerPlayerEntity,
        npcName: String,
        llmType: String,
        llmModel: String
    ): NPC {
        return when (llmType) {
            LLMType.OLLAMA.name -> {
                val llmClient = initOllamaClient(llmModel)
                val (controller, history) = initBase(llmClient, server, npcEntity, npcName)
                val functionManager = OllamaFunctionManager(resourcesProvider!!, controller, npcEntity, history, llmClient)
                val eventHandler = NPCEventHandler(llmClient, history, functionManager)
                NPC(npcEntity, llmClient, history, eventHandler)
            }

            LLMType.OPENAI.name -> {
                val llmClient = initOpenAiClient(llmModel)
                val (controller, history) = initBase(llmClient, server, npcEntity, npcName)
                val functionManager = OpenAiFunctionManager(resourcesProvider!!, controller, npcEntity, history, llmClient)
                val eventHandler = NPCEventHandler(llmClient, history, functionManager)
                NPC(npcEntity, llmClient, history, eventHandler)
            }
            else -> throw NPCCreationException("Invalid llm type: $llmType")
        }
    }

    private fun initBase(
        llmClient: ILLMClient,
        server: MinecraftServer,
        npcEntity: ServerPlayerEntity,
        npcName: String
    ): Pair<NPCController, ConversationHistory> {
        initResourceProvider(llmClient, server, npcName)
        val baritone = BaritoneAPI.getProvider().getBaritone(npcEntity)
        val controller = NPCController(npcEntity, baritone)
        val history = ConversationHistory(resourcesProvider!!, npcName)
        return Pair(controller, history)
    }

    private fun handleInitMessage(eventHandler: IEventHandler, npcName: String) {
        eventHandler.onEvent(StringUtils.EMPTY, Instructions.getDefaultInstruction(npcName))
    }

    private fun initOpenAiClient(openAiModel: String): OpenAiClient {
        val apiKey = config.getProperty(ConfigConstants.NPC_LLM_OPENAI_API_KEY)
        val baseUrl = config.getProperty(ConfigConstants.NPC_LLM_OPENAI_BASE_URL)
        return OpenAiClient(openAiModel, apiKey, baseUrl)
    }

    private fun initOllamaClient(ollamaModel: String): OllamaClient {
        val ollamaUrl = config.getProperty(ConfigConstants.NPC_LLM_OLLAMA_URL)
        val llmService = OllamaClient(ollamaModel, ollamaUrl)
        llmService.checkServiceIsReachable()
        return llmService
    }

    private fun initResourceProvider(llmClient: ILLMClient, server: MinecraftServer, npcName: String) {
        this.resourcesProvider ?: run {
            this.resourcesProvider = ResourcesProvider(
                repositoryFactory.conversationRepository,
                repositoryFactory.recipesRepository,
                llmClient
            )
            resourcesProvider?.loadResources(server, npcName)
        }
    }

}
