package io.sailex.ai.npc.client.listener

import io.sailex.ai.npc.client.model.NPC
import io.sailex.ai.npc.client.model.NPCEvent
import io.sailex.ai.npc.client.model.interaction.ActionType
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

abstract class AEventListener(val npc: NPC) : IEventListener {

    protected val logger: Logger = LogManager.getLogger()

    abstract override fun register()

    protected fun handleMessage(type: ActionType, message: String) {
        npc.npcController.handleEvent(NPCEvent(type, message))
    }
}