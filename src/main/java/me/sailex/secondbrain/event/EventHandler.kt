package me.sailex.secondbrain.event

import me.sailex.secondbrain.llm.roles.ChatRole

interface EventHandler {

    fun onEvent(role: ChatRole, prompt: String, formatPrompt: Boolean)
    fun onEvent(prompt: String)
    fun stopService()
}