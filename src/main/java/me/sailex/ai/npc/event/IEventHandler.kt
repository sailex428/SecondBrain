package me.sailex.ai.npc.event

interface IEventHandler {

    fun onEvent(source: String, prompt: String)
    fun stopService()
}