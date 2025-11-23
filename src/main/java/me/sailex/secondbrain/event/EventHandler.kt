package me.sailex.secondbrain.event

interface EventHandler {

    fun onEvent(prompt: String)
    fun stopService()
    fun queueIsEmpty(): Boolean
}