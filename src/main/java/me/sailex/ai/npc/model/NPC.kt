package me.sailex.ai.npc.model

import me.sailex.ai.npc.event.IEventHandler
import me.sailex.ai.npc.history.ConversationHistory
import me.sailex.ai.npc.llm.ILLMClient
import net.minecraft.server.network.ServerPlayerEntity

data class NPC(
    val entity: ServerPlayerEntity,
    val llmClient: ILLMClient,
    val history: ConversationHistory,
    val eventHandler: IEventHandler
)
