package me.sailex.ai.npc.listener

import me.sailex.ai.npc.npc.NPC
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

abstract class AEventListener(
    protected val npc: NPC
) : IEventListener {
    private val logger: Logger = LogManager.getLogger()

    abstract override fun register()

    protected fun handleMessage(message: String) {
        logger.info(message)
        npc.controller.handleEvent(message)
    }
}
