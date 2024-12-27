package io.sailex.ai.npc.client.listeners

import io.sailex.ai.npc.client.model.NPC
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

abstract class AEventListener(
    val npc: NPC,
) : IEventListener {
    protected val logger: Logger = LogManager.getLogger()

    abstract override fun register()

    protected fun handleMessage(message: String) {
        npc.npcController.handleEvent(message)
    }
}
