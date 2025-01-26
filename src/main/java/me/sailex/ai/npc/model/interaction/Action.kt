package me.sailex.ai.npc.model.interaction

import me.sailex.ai.npc.model.context.WorldContext

/**
 * Represents an action that can be taken by the NPC
 */
data class Action(
    val action: ActionType,
    val message: String?,
    val targetId: String?,
    val targetType: String?,
    val targetPosition: WorldContext.Position
)
