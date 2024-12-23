package io.sailex.ai.npc.client.model

import io.sailex.ai.npc.client.context.ContextGenerator
import io.sailex.ai.npc.client.llm.ILLMClient
import io.sailex.ai.npc.client.npc.NPCController
import net.minecraft.client.network.ClientPlayerEntity
import java.util.UUID

/**
 * Represents an NPC in the game (client player entity) and its llm service, context generator, and controller
 */
class NPC(
    val id: UUID,
    val npcEntity: ClientPlayerEntity,
    val npcController: NPCController,
    val contextGenerator: ContextGenerator,
    val llmService: ILLMClient,
)
