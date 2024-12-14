package io.sailex.aiNpc.client.listener

import io.sailex.aiNpc.client.model.NPC
import io.sailex.aiNpc.client.model.NPCEvent
import io.sailex.aiNpc.client.model.interaction.ActionType
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

abstract class AEventListener(val npc: NPC) : IEventListener {

    protected val logger: Logger = LogManager.getLogger()

    abstract override fun register()

    protected fun handleMessage(type: ActionType, message: String) {
        npc.npcController.handleEvent(NPCEvent(type, message))
    }
}