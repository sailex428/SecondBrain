package io.sailex.ai.npc.client.model.interaction

import io.sailex.ai.npc.client.model.database.ActionResource
import io.sailex.ai.npc.client.model.database.Conversation
import io.sailex.ai.npc.client.model.database.Requirement

data class Resources(
    val actionResources: List<ActionResource>,
    val requirements: List<Requirement>,
    val conversations: List<Conversation>
)
