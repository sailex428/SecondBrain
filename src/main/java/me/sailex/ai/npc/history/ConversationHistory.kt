package me.sailex.ai.npc.history

import me.sailex.ai.npc.database.resources.ResourcesProvider
import java.sql.Timestamp
import java.util.concurrent.LinkedBlockingDeque

class ConversationHistory(
    private val resourcesProvider: ResourcesProvider,
    private val npcName: String
) {
    private val latestConversations = LinkedBlockingDeque<Pair<Timestamp, String>>(4) //timestamp to message

    fun add(message: String) {
        if (message.isEmpty()) return
        if (latestConversations.remainingCapacity() == 0) {
            val oldestConversation = latestConversations.takeLast()
            resourcesProvider.addConversation(npcName, oldestConversation.first, oldestConversation.second)
        }
        val currentTime = Timestamp(System.currentTimeMillis())
        latestConversations.add(Pair(currentTime, message))
    }

    fun getFormattedConversation(): String {
        return latestConversations.joinToString { conversation -> "${conversation.first}: ${conversation.second}" }
    }
}