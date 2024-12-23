package io.sailex.ai.npc.client.listeners

import io.sailex.ai.npc.client.database.SqliteClient
import io.sailex.ai.npc.client.model.NPC
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import net.minecraft.client.MinecraftClient

/**
 * Manager for handling event listeners.
 * Registers listeners for block interactions, chat messages, and stopping the client.
 */
class EventListenerRegisterer(
    private val npc: NPC,
) {
    /**
     * Register the event listeners.
     */
    fun registerListeners(sqliteClient: SqliteClient) {
        listOf<IEventListener>(
            BlockInteractionListener(npc),
            EntityListener(npc),
            ChatMessageListener(npc),
        ).forEach { listener -> listener.register() }

        registerStoppingListener(sqliteClient)
    }

    private fun registerStoppingListener(sqliteClient: SqliteClient) {
        ClientLifecycleEvents.CLIENT_STOPPING.register(
            ClientLifecycleEvents.ClientStopping { _: MinecraftClient? ->
                stopServices(
                    sqliteClient,
                )
            },
        )
    }

    private fun stopServices(sqliteClient: SqliteClient) {
        npc.llmService.stopService()
        npc.contextGenerator.stopService()
        sqliteClient.closeConnection()
    }
}
