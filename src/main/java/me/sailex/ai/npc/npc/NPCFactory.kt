package me.sailex.ai.npc.npc

import baritone.api.BaritoneAPI
import me.sailex.ai.npc.config.ModConfig
import me.sailex.ai.npc.constant.ConfigConstants
import me.sailex.ai.npc.database.repositories.RepositoryFactory
import me.sailex.ai.npc.database.resources.ResourcesProvider
import me.sailex.ai.npc.exception.InvalidLLMTypeException
import me.sailex.ai.npc.history.ConversationHistory
import me.sailex.ai.npc.listener.EventListenerRegisterer
import me.sailex.ai.npc.llm.ILLMClient
import me.sailex.ai.npc.llm.LLMType
import me.sailex.ai.npc.llm.OllamaClient
import me.sailex.ai.npc.llm.OpenAiClient
import me.sailex.ai.npc.llm.function_calling.OpenAiFunctionManager
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity

class NPCFactory(
    private val config: ModConfig,
    private val repositoryFactory: RepositoryFactory
) {
    private val nameToNpc = mutableMapOf<String, NPC>()
    var resourcesProvider: ResourcesProvider? = null
        private set

    fun createNpc(server: MinecraftServer, npcEntity: ServerPlayerEntity, llmType: String, llmModel: String) {
        val npcName = npcEntity.name.string
        val llmClient = initLlmClient(llmType, llmModel)

        if (this.resourcesProvider == null) {
            this.resourcesProvider = ResourcesProvider(
                repositoryFactory.conversationRepository,
                repositoryFactory.recipesRepository,
                llmClient
            )
            this.resourcesProvider?.loadResources(server, npcName)
        }

        val baritone = BaritoneAPI.getProvider().getBaritone(npcEntity)
        val history = ConversationHistory(resourcesProvider!!, npcName)
        val controller = NPCController(npcEntity, baritone, llmClient, history)
        llmClient.setFunctionManager(OpenAiFunctionManager(controller, resourcesProvider!!, npcEntity))

        val npc = NPC(npcEntity, llmClient, controller)

        //start event listening
        val eventListenerRegisterer = EventListenerRegisterer(npc)
        eventListenerRegisterer.registerListeners()

        nameToNpc.put(npcName, npc)
    }

    fun removeNpc(npcName: String): Boolean {
        val npcToRemove = nameToNpc[npcName]
        if (npcToRemove != null) {
            npcToRemove.llmClient.stopService()
            npcToRemove.controller.stopService()
            nameToNpc.remove(npcName)
            return true
        }
        return false
    }

    fun shutdownNpc() {
        nameToNpc.values.forEach {
            it.controller.stopService()
            it.llmClient.stopService()
        }
    }

    private fun initLlmClient(llmType: String, llmModel: String): ILLMClient {
        return if (LLMType.OLLAMA.name == llmType) {
            initOllamaClient(llmModel)
        } else if (LLMType.OPENAI.name == llmType) {
            initOpenAiClient(llmModel)
        } else {
            throw InvalidLLMTypeException("Invalid llm type: $llmType")
        }
    }

    private fun initOpenAiClient(openAiModel: String): ILLMClient {
        val apiKey = config.getProperty(ConfigConstants.NPC_LLM_OPENAI_API_KEY)
        val baseUrl = config.getProperty(ConfigConstants.NPC_LLM_OPENAI_BASE_URL)
        return OpenAiClient(openAiModel, apiKey, baseUrl)
    }

    private fun initOllamaClient(ollamaModel: String): ILLMClient {
        val ollamaUrl = config.getProperty(ConfigConstants.NPC_LLM_OLLAMA_URL)
        val llmService = OllamaClient(ollamaModel, ollamaUrl)
        llmService.checkServiceIsReachable()
        return llmService
    }

}