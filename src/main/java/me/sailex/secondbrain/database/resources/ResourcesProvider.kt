package me.sailex.secondbrain.database.resources

import me.sailex.secondbrain.database.repositories.ConversationRepository
import me.sailex.secondbrain.llm.LLMClient
import me.sailex.secondbrain.model.database.Conversation
import me.sailex.secondbrain.util.LogUtil
import me.sailex.secondbrain.util.ResourceRecommender

import java.sql.Timestamp
import java.util.concurrent.CompletableFuture

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class ResourcesProvider(
    private val conversationRepository: ConversationRepository,
    val llmClient: LLMClient
) {
    private var executorService: ExecutorService = initExecutorPool()

    private val conversations = arrayListOf<Conversation>()

    /**
     * Loads conversations recipes from db/mc into memory
     */
    fun loadResources(npcName: String) {
        runAsync {
            LogUtil.info("Loading conversations into memory...")
            this.conversations.addAll(conversationRepository.selectByName(npcName))
            executorService.shutdown()
        }
    }

    fun getRelevantConversations(message: String): List<Conversation> {
        return ResourceRecommender.getRelevantResources(llmClient, message, conversations, 3)
            .filterIsInstance<Conversation>()
    }

    fun addConversation(npcName: String, message: String) {
        this.conversations.add(Conversation(
            npcName, message,
            Timestamp(System.currentTimeMillis()),
            llmClient.generateEmbedding(listOf(message))))
    }

    /**
     * Saves recipes and conversations to local db. (called on server stop)
     *
     * Stops initial resources indexing if not finished by shutting down executor
     */
    fun saveResources() {
        shutdownServiceNow()
        executorService = initExecutorPool()
        runAsync {
            conversations.forEach { conversation -> conversationRepository.insert(conversation) }
            LogUtil.info("Saved conversations to db")
        }.get()
        executorService.shutdownNow()
    }

    private fun shutdownServiceNow() {
        if (!executorService.isTerminated) {
            executorService.shutdownNow()
            LogUtil.error("Initial loading of resources interrupted - Wait for termination")
            executorService.awaitTermination(500, TimeUnit.MILLISECONDS)
        }
    }

    private fun runAsync(task: () -> Unit): CompletableFuture<Void> {
        return CompletableFuture.runAsync(task , executorService).exceptionally {
            LogUtil.error("Error loading/saving resources into memory", it)
            null
        }
    }

    private fun initExecutorPool(): ExecutorService {
        return Executors.newFixedThreadPool(2)
    }
}
