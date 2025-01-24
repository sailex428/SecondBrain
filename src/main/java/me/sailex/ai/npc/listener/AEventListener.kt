package me.sailex.ai.npc.listener

import me.sailex.ai.npc.npc.NPC
import me.sailex.ai.npc.util.LogUtil

abstract class AEventListener(
    protected val npc: NPC
) : IEventListener {

    abstract override fun register()

    protected fun handleMessage(userPrompt: String, systemPrompt: String) {
        LogUtil.info("userPrompt: $userPrompt; systemPrompt: $systemPrompt", true)
        npc.controller.onEvent(userPrompt, systemPrompt)
    }
}
