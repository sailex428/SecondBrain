package me.sailex.secondbrain.history

import me.sailex.secondbrain.database.resources.ResourcesProvider
import me.sailex.secondbrain.llm.function_calling.model.ChatMessage
import me.sailex.secondbrain.llm.roles.ChatRole
import java.util.concurrent.LinkedBlockingDeque

class ConversationHistory(
    private val resourcesProvider: ResourcesProvider?,
    private val npcName: String
) {
    private val latestConversations = LinkedBlockingDeque<ChatMessage>(4)

    fun add(role: ChatRole, content: String) {
        if (content.isEmpty()) return
        if (latestConversations.remainingCapacity() == 0) {
            val oldestConversation = latestConversations.takeFirst()
            resourcesProvider?.addConversation(npcName, oldestConversation.content())
        }
        latestConversations.add(ChatMessage.of(role, content))
    }

    fun getConversations(): List<ChatMessage> {
        return latestConversations.toList()
    }
}