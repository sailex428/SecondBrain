package me.sailex.ai.npc.npc

import me.sailex.ai.npc.context.ContextGenerator
import me.sailex.ai.npc.llm.ILLMClient
import net.minecraft.server.network.ServerPlayerEntity

/**
 * Represents an NPC in the game and its llm service, context generator, and controller
 */
class NPC(
    val entity: ServerPlayerEntity,
    val contextGenerator: ContextGenerator,
    val llmClient: ILLMClient,
    val controller: NPCController,
)
