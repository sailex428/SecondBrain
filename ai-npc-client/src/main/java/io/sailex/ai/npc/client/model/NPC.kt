package io.sailex.ai.npc.client.model

import io.sailex.ai.npc.client.llm.ILLMClient
import io.sailex.ai.npc.client.npc.NPCContextGenerator
import io.sailex.ai.npc.client.npc.NPCController
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
