package me.sailex.ai.npc.event

interface IEventHandler {

    fun onEvent(prompt: String)
    fun stopService()
}