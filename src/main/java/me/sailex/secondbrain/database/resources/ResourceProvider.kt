package me.sailex.secondbrain.database.resources

import me.sailex.secondbrain.database.repositories.ConversationRepository
import me.sailex.secondbrain.history.Message
import me.sailex.secondbrain.model.database.Conversation
import me.sailex.secondbrain.util.LogUtil
import java.util.UUID

import java.util.concurrent.CompletableFuture

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class ResourceProvider(
    val conversationRepository: ConversationRepository
) {
    private lateinit var executorService: ExecutorService
    val loadedConversations = hashMapOf<UUID, List<Conversation>>()

    /**
     * Loads conversations recipes from db/mc into memory
     */
    fun loadResources(uuids: List<UUID>) {
        executorService = initExecutorPool()
        runAsync {
            LogUtil.info("Loading conversations into memory...")
            uuids.forEach {
                this.loadedConversations[it] = conversationRepository.selectByUuid(it)
            }
            executorService.shutdownNow()
        }
    }

    fun addConversations(uuid: UUID, messages: List<Message>) {
        this.loadedConversations[uuid] = messages.map { Conversation(uuid, it.role, it.message) }
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
            loadedConversations.forEach { conversations ->
                conversations.value.forEach { conversationRepository.insert(it) } }
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
