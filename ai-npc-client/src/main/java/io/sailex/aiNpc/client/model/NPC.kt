package io.sailex.aiNpc.client.model

import io.sailex.aiNpc.client.llm.ILLMClient
import io.sailex.aiNpc.client.npc.NPCContextGenerator
import io.sailex.aiNpc.client.npc.NPCController
import net.minecraft.client.network.ClientPlayerEntity
import java.util.*

/**
 * Represents an NPC in the game and its llm service, context generator, and controller
 */
class NPC(
    val id: UUID,
    val npcEntity: ClientPlayerEntity,
    val npcController: NPCController,
    val npcContextGenerator: NPCContextGenerator,
    val llmService: ILLMClient
)
