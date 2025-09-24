package me.sailex.secondbrain.event

import me.sailex.secondbrain.llm.roles.Player2ChatRole

interface EventHandler {

    fun onEvent(role: Player2ChatRole, prompt: String)
    fun onEvent(prompt: String)
    fun stopService()
    fun queueIsEmpty(): Boolean
}