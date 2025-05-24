package me.sailex.secondbrain.history

import me.sailex.secondbrain.database.resources.ResourcesProvider
import me.sailex.secondbrain.model.function_calling.FunctionResponse
import java.sql.Timestamp
import java.util.concurrent.LinkedBlockingDeque
import java.util.stream.Collectors

class ConversationHistory(
    private val resourcesProvider: ResourcesProvider?,
    private val npcName: String
) {
    private val latestConversations = LinkedBlockingDeque<Pair<Timestamp, String>>(4) //timestamp to message

    fun add(prompt: String, response: FunctionResponse) {
        add("prompt: $prompt" + " response: " + response.finalResponse + " - " + response.toolCalls)
    }

    fun add(message: String) {
        if (latestConversations.remainingCapacity() == 0) {
            val oldestConversation = latestConversations.takeFirst()
            resourcesProvider?.addConversation(npcName, oldestConversation.first, oldestConversation.second)
        }
        val currentTime = Timestamp(System.currentTimeMillis())
        latestConversations.add(Pair(currentTime, message))
    }

    fun getFormattedHistory(): String {
        return "Latest Conversations: " + latestConversations.stream()
            .map { pair -> pair.second }
            .collect(Collectors.joining("\n"))
    }
}