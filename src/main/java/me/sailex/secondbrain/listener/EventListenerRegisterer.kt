package me.sailex.secondbrain.listener

import me.sailex.secondbrain.model.NPC
import java.util.UUID

/**
 * Registers listeners for block interactions, chat messages and so on.
 */
class EventListenerRegisterer(
    private val npcs: Map<UUID, NPC>
) {
    /**
     * Register the event listeners.
     */
    fun register() {
        listOf<IEventListener>(
//            BlockInteractionListener(npcs),
            EntityLoadListener(npcs),
            ChatMessageListener(npcs),
            CombatEventListener(npcs)
        ).forEach { listener -> listener.register() }
    }
}
