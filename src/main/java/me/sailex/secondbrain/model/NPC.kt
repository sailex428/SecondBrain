package me.sailex.secondbrain.model

import me.sailex.altoclef.AltoClefController
import me.sailex.secondbrain.config.NPCConfig
import me.sailex.secondbrain.context.ContextProvider
import me.sailex.secondbrain.event.EventHandler
import me.sailex.secondbrain.history.ConversationHistory
import me.sailex.secondbrain.llm.LLMClient
import net.minecraft.server.network.ServerPlayerEntity

data class NPC(
    val entity: ServerPlayerEntity,
    val llmClient: LLMClient,
    val history: ConversationHistory,
    val eventHandler: EventHandler,
    val controller: AltoClefController,
    val contextProvider: ContextProvider,
    val config: NPCConfig
)