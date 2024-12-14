package io.sailex.aiNpc.client.listener

import io.sailex.aiNpc.client.database.SqliteClient
import io.sailex.aiNpc.client.model.NPC
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import net.minecraft.client.MinecraftClient

/**
 * Manager for handling event listeners.
 * Registers listeners for block interactions, chat messages, and stopping the client.
 */
class EventListenerManager(private val npc: NPC) {
    /**
     * Register the event listeners.
     */
    fun registerListeners(sqliteClient: SqliteClient) {
        listOf<IEventListener>(
            BlockInteractionListener(npc),
            EntityListener(npc),
            ChatMessageListener(npc)
        ).forEach { listener -> listener.register() }

        registerStoppingListener(sqliteClient)
    }

    private fun registerStoppingListener(sqliteClient: SqliteClient) {
        ClientLifecycleEvents.CLIENT_STOPPING.register(ClientLifecycleEvents.ClientStopping { client: MinecraftClient? ->
            stopServices(
                sqliteClient
            )
        })
    }

    private fun stopServices(sqliteClient: SqliteClient) {
        npc.llmService.stopService()
        npc.npcContextGenerator.stopService()
        sqliteClient.closeConnection()
    }
}
