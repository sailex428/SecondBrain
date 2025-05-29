package me.sailex.secondbrain.event

import me.sailex.secondbrain.llm.roles.ChatRole

interface EventHandler {

    fun onEvent(role: ChatRole, prompt: String)
    fun onEvent(prompt: String)
    fun stopService()
}