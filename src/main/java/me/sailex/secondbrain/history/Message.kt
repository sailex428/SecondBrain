package me.sailex.secondbrain.history

data class Message(
    val message: String,
    val role: String,
    val tools: List<ToolCall>? = emptyList()
)
