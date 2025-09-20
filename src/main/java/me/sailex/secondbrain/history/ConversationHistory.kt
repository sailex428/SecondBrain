package me.sailex.secondbrain.history

import com.fasterxml.jackson.databind.ObjectMapper
import me.sailex.secondbrain.constant.Instructions
import me.sailex.secondbrain.llm.LLMClient

class ConversationHistory(
    private val llmClient: LLMClient,
    initMessage: String
) {
    companion object {
        private const val MAX_HISTORY_LENGTH = 30
        private val objectMapper = ObjectMapper()
    }
    val latestConversations = mutableListOf<Message>()

    init {
        setInitMessage(initMessage)
    }

    @Synchronized
    fun add(message: Message) {
        latestConversations.add(message)

        if (latestConversations.size >= MAX_HISTORY_LENGTH) {
            updateConversations()
        }
    }

    private fun updateConversations() {
        val removeCount = MAX_HISTORY_LENGTH / 3
        val toSummarize = latestConversations.subList(1, removeCount).toList()
        val message = summarize(toSummarize)
        latestConversations.removeAll(toSummarize)
        latestConversations.add(1, message)
    }

    private fun summarize(conversations: List<Message>): Message {
        val summarizeMessage = Message(
            Instructions.SUMMARY + objectMapper.writeValueAsString(conversations),
            "user")
        return llmClient.chat(summarizeMessage)
    }

    private fun setInitMessage(initMessage: String) {
        latestConversations.add(0, Message(initMessage, "system"))
    }
}