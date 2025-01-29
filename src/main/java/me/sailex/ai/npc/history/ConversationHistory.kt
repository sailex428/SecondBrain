package me.sailex.ai.npc.history

import me.sailex.ai.npc.model.database.Conversation
import java.util.concurrent.LinkedBlockingDeque

class ConversationHistory {

    val latestConversations = LinkedBlockingDeque<Conversation>(8)
        private set
    private val allConversations = arrayListOf<Conversation>()

    fun add(conversation: Conversation) {
        if (latestConversations.remainingCapacity() == 0) {
            val oldestConversation = latestConversations.takeLast()
            allConversations.add(oldestConversation)
        }
        latestConversations.add(conversation)
    }

    fun getLatestConversation(): List<Conversation> {
        return latestConversations.toList()
    }

}