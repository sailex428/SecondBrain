package me.sailex.ai.npc.npc

import me.sailex.ai.npc.history.ConversationHistory
import me.sailex.ai.npc.llm.ILLMClient
import net.minecraft.server.network.ServerPlayerEntity

data class NPC<T>(
    val entity: ServerPlayerEntity,
    val llmClient: ILLMClient,
    val history: ConversationHistory,
    val eventHandler: NPCEventHandler<T>
)
