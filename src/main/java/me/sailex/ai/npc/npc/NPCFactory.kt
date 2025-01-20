package me.sailex.ai.npc.npc

import baritone.api.BaritoneAPI
import me.sailex.ai.npc.config.ModConfig
import me.sailex.ai.npc.constant.ConfigConstants
import me.sailex.ai.npc.database.SqliteClient
import me.sailex.ai.npc.database.indexer.DefaultResourcesIndexer
import me.sailex.ai.npc.database.repositories.RepositoryFactory
import me.sailex.ai.npc.exception.InvalidLLMTypeException
import me.sailex.ai.npc.listener.EventListenerRegisterer
import me.sailex.ai.npc.llm.ILLMClient
import me.sailex.ai.npc.llm.LLMType
import me.sailex.ai.npc.llm.OllamaClient
import me.sailex.ai.npc.llm.OpenAiClient
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity

class NPCFactory(
    private val config: ModConfig,
    private val sqliteClient: SqliteClient
) {
    private val nameToNpc = mutableMapOf<String, NPC>()

    fun createNpc(server: MinecraftServer, npcEntity: ServerPlayerEntity, llmType: String, llmModel: String) {
        val llmClient = initLlmClient(llmType, llmModel)
        val repositoryFactory = RepositoryFactory(llmClient, sqliteClient)
        repositoryFactory.initRepositories()

        val resourcesIndexer = DefaultResourcesIndexer(repositoryFactory.recipesRepository,
            repositoryFactory.skillRepository,
            repositoryFactory.blockRepository,
            llmClient)
        resourcesIndexer.indexAll(server)

        val baritone = BaritoneAPI.getProvider().getBaritone(npcEntity)
        val controller = NPCController(npcEntity, llmClient, repositoryFactory, baritone)
        controller.start()

        val npc = NPC(npcEntity, llmClient, controller)

        val eventListenerRegisterer = EventListenerRegisterer(npc)
        eventListenerRegisterer.registerListeners()
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