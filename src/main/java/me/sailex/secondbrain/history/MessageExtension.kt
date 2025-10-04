@file:JvmName("MessageConverter")
package me.sailex.secondbrain.history

import io.github.ollama4j.models.chat.OllamaChatMessage
import io.github.ollama4j.models.chat.OllamaChatMessageRole
import io.github.sashirestela.openai.domain.chat.ChatMessage
import me.sailex.secondbrain.llm.player2.model.Player2ChatMessage
import me.sailex.secondbrain.llm.player2.model.Player2ResponseMessage
import me.sailex.secondbrain.llm.roles.Player2ChatRole

// player2
fun Player2ResponseMessage.toMessage(): Message = Message(
    this.content,
    this.role.toString().lowercase()
)

fun Message.toPlayer2ChatMessage(): Player2ChatMessage = Player2ChatMessage(
    Player2ChatRole.valueOf(this.role.uppercase()),
    this.message
)

// ollama
fun OllamaChatMessage.toMessage(): Message = Message(
    this.content,
    this.role.toString().lowercase()
)

fun Message.toOllamaChatMessage(): OllamaChatMessage = OllamaChatMessage(
    OllamaChatMessageRole.getRole(this.role),
    this.message
)

//openai
fun ChatMessage.ResponseMessage.toMessage(): Message {
    return Message(this.content, this.role.toString().lowercase())
}

fun Message.toChatMessage(): ChatMessage {
    val role = ChatMessage.ChatRole.valueOf(this.role.uppercase())
    return if (role == ChatMessage.ChatRole.SYSTEM) {
        ChatMessage.SystemMessage.of(this.message)
    } else {
        ChatMessage.UserMessage.of(this.message)
    }
}
