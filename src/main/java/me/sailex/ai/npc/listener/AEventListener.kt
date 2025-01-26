package me.sailex.ai.npc.listener

import me.sailex.ai.npc.npc.NPC
import me.sailex.ai.npc.util.LogUtil

abstract class AEventListener(
    protected val npc: NPC
) : IEventListener {

    abstract override fun register()

    protected fun handleMessage(source: String, prompt: String) {
        LogUtil.info("source: $source; prompt: $prompt", true)
        npc.controller.onEvent(source, prompt)
    }
}
