package me.sailex.ai.npc.npc

import baritone.api.BaritoneAPI
import io.github.ollama4j.tools.Tools
import io.github.sashirestela.openai.common.function.FunctionDef
import me.sailex.ai.npc.config.ModConfig
import me.sailex.ai.npc.constant.ConfigConstants
import me.sailex.ai.npc.database.repositories.RepositoryFactory
import me.sailex.ai.npc.database.resources.ResourcesProvider
import me.sailex.ai.npc.exception.InvalidLLMTypeException
import me.sailex.ai.npc.history.ConversationHistory
import me.sailex.ai.npc.listener.EventListenerRegisterer
import me.sailex.ai.npc.llm.IFunctionCaller
import me.sailex.ai.npc.llm.ILLMClient
import me.sailex.ai.npc.llm.LLMType
import me.sailex.ai.npc.llm.OllamaClient
import me.sailex.ai.npc.llm.OpenAiClient
import me.sailex.ai.npc.llm.function_calling.OllamaFunctionManager
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

    fun createNpc(
        server: MinecraftServer,
        npcEntity: ServerPlayerEntity,
        llmType: String,
        llmModel: String
    ) {
        val npcName = npcEntity.name.string
        val llmClient = initLlmClient(llmType, llmModel)

        initResourceProvider(llmClient, server, npcName)
        val baritone = BaritoneAPI.getProvider().getBaritone(npcEntity)
        val controller = NPCController(npcEntity, baritone)
        val history = ConversationHistory(resourcesProvider!!, npcName)

        if (llmClient is OpenAiClient) {
            val functionManager = OpenAiFunctionManager(resourcesProvider!!, controller, npcEntity, history)
            val eventHandler = NPCEventHandler<FunctionDef>(llmClient, history, functionManager)
            return NPC(npcEntity, llmClient, history, eventHandler)
        } else if (llmClient is OllamaClient) {
            val functionManager = OllamaFunctionManager(resourcesProvider!!, controller, npcEntity, history)
            val eventHandler = NPCEventHandler<Tools.ToolSpecification>(llmClient, history, functionManager)
            return NPC(npcEntity, llmClient, history, eventHandler)
        }

        //start event listening
        val eventListenerRegisterer = EventListenerRegisterer(npc)
        eventListenerRegisterer.register()
        controller.tick()

        nameToNpc.put(npcName, npc)
    }

    fun removeNpc(npcName: String): Boolean {
        val npcToRemove = nameToNpc[npcName]
        if (npcToRemove != null) {
            npcToRemove.llmClient.stopService()
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
        return when (llmType) {
            LLMType.OLLAMA.name -> initOllamaClient(llmModel)
            LLMType.OPENAI.name -> initOpenAiClient(llmModel)
            else -> throw InvalidLLMTypeException("Invalid llm type: $llmType")
        }
    }

    private fun initOpenAiClient(openAiModel: String): IFunctionCaller<FunctionDef> {
        val apiKey = config.getProperty(ConfigConstants.NPC_LLM_OPENAI_API_KEY)
        val baseUrl = config.getProperty(ConfigConstants.NPC_LLM_OPENAI_BASE_URL)
        return OpenAiClient(openAiModel, apiKey, baseUrl)
    }

    private fun initOllamaClient(ollamaModel: String): IFunctionCaller<Tools.ToolSpecification> {
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
