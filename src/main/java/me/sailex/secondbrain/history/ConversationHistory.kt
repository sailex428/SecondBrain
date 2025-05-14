package me.sailex.secondbrain.history

import me.sailex.secondbrain.database.resources.ResourcesProvider
import java.sql.Timestamp
import java.util.concurrent.LinkedBlockingDeque
import java.util.stream.Collectors

class ConversationHistory(
    private val resourcesProvider: ResourcesProvider?,
    private val npcName: String
) {
    private val latestConversations = LinkedBlockingDeque<Pair<Timestamp, String>>(4) //timestamp to message

    fun add(message: String) {
        if (message.isBlank()) return
        if (latestConversations.remainingCapacity() == 0) {
            val oldestConversation = latestConversations.takeLast()
            resourcesProvider?.addConversation(npcName, oldestConversation.first, oldestConversation.second)
        }
        val currentTime = Timestamp(System.currentTimeMillis())
        latestConversations.add(Pair(currentTime, message))
    }

    fun getFormattedHistory(): String {
        return latestConversations.stream().map { pair -> pair.second }.collect(Collectors.joining("\n"))
    }
}