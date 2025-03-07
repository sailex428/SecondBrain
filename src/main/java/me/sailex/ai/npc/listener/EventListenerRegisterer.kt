package me.sailex.ai.npc.listener

import me.sailex.ai.npc.model.NPC

/**
 * Registers listeners for block interactions, chat messages and so on.
 */
class EventListenerRegisterer(
    private val npcs: Map<String, NPC>
) {
    /**
     * Register the event listeners.
     */
    fun register() {
        listOf<IEventListener>(
            BlockInteractionListener(npcs),
            EntityLoadListener(npcs),
            ChatMessageListener(npcs),
            CombatEventListener(npcs)
        ).forEach { listener -> listener.register() }
    }
}
