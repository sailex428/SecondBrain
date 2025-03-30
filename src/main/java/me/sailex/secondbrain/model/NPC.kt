package me.sailex.secondbrain.model

import me.sailex.secondbrain.common.NPCController
import me.sailex.secondbrain.config.NPCConfig
import me.sailex.secondbrain.context.ContextProvider
import me.sailex.secondbrain.event.EventHandler
import me.sailex.secondbrain.history.ConversationHistory
import me.sailex.secondbrain.llm.LLMClient
import me.sailex.secondbrain.mode.ModeController
import net.minecraft.server.network.ServerPlayerEntity

data class NPC(
    val entity: ServerPlayerEntity,
    val llmClient: LLMClient,
    val history: ConversationHistory,
    val eventHandler: EventHandler,
    val npcController: NPCController,
    val contextProvider: ContextProvider,
    val modeController: ModeController,
    val config: NPCConfig
)