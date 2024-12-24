package io.sailex.ai.npc.client.model.interaction

import io.sailex.ai.npc.client.model.context.WorldContext

/**
 * Represents an action that can be taken by the NPC
 */
data class Action(
    val action: ActionType,
    val message: String?,
    val targetId: String?,
    val targetType: String?,
    val targetPosition: WorldContext.Position,
)
