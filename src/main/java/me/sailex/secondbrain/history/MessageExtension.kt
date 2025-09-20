@file:JvmName("MessageConverter")
package me.sailex.secondbrain.history

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import io.github.ollama4j.models.chat.OllamaChatMessage
import io.github.ollama4j.models.chat.OllamaChatMessageRole
import io.github.ollama4j.models.chat.OllamaChatToolCalls
import io.github.ollama4j.tools.OllamaToolCallsFunction
import io.github.sashirestela.openai.common.function.FunctionCall
import me.sailex.secondbrain.llm.player2.model.ChatMessage
import me.sailex.secondbrain.llm.player2.model.ResponseMessage
import me.sailex.secondbrain.llm.roles.ChatRole

// player2/openai
fun ResponseMessage.toMessage(): Message = Message(
    this.content,
    this.role.toString(),
    this.tool_calls.map { toToolCall(it) },
)

fun Message.toChatMessage(): ChatMessage = ChatMessage(
    ChatRole.valueOf(this.role),
    "${this.message} - TOOL_CALLS: ${this.tools?.joinToString { toString(it) }}"
)

// ollama
fun OllamaChatMessage.toMessage(): Message = Message(
    this.content,
    this.role.toString(),
    this.toolCalls.map { toToolCall(it) }
)

fun Message.toOllamaChatMessage(): OllamaChatMessage = OllamaChatMessage(
    OllamaChatMessageRole.getRole(this.role),
    this.message,
    this.tools?.map { toOllamaToolCallsFunction(it) },
    emptyList()
)

val objectMapper = ObjectMapper()

fun toToolCall(toolCall: OllamaChatToolCalls): ToolCall {
    val arguments = objectMapper.writeValueAsString(toolCall.function.arguments)
    return ToolCall( null, toolCall.function.name, arguments)
}

fun toToolCall(toolCall: me.sailex.secondbrain.llm.player2.model.ToolCall): ToolCall {
    val arguments = objectMapper.writeValueAsString(toolCall.function.arguments)
    return ToolCall( toolCall.id, toolCall.function.name, arguments)
}

fun toOllamaToolCallsFunction(toolCall: ToolCall): OllamaChatToolCalls {
    val argumentsMap: Map<String, Object> = try {
        objectMapper.readValue(toolCall.arguments, object : TypeReference<Map<String, Object>>() {})
    } catch (_: Exception) {
        emptyMap()
    }

    return OllamaChatToolCalls(OllamaToolCallsFunction(
        toolCall.name,
        argumentsMap,
    ))
}

fun toPlayer2ToolCall(toolCall: ToolCall): me.sailex.secondbrain.llm.player2.model.ToolCall {
    return me.sailex.secondbrain.llm.player2.model.ToolCall(
        0,
        toolCall.id,
        "function",
        FunctionCall(toolCall.name, toolCall.arguments)
    )
}

fun toString(toolCall: ToolCall): String {
    return "${toolCall.name}: ${toolCall.arguments}; "
}