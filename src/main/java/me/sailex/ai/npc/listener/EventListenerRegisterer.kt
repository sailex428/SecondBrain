package me.sailex.ai.npc.listener

import me.sailex.ai.npc.model.NPC

/**
 * Manager for handling event listeners.
 * Registers listeners for block interactions, chat messages, and stopping the client.
 */
class EventListenerRegisterer(
    private val npc: NPC
) {
    /**
     * Register the event listeners.
     */
    fun register() {
        listOf<IEventListener>(
            BlockInteractionListener(npc),
            EntityLoadListener(npc),
            ChatMessageListener(npc),
            CombatEventListener(npc),
        ).forEach { listener -> listener.register() }
    }
}
