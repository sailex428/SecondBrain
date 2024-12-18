package io.sailex.ai.npc.client.model.interaction

import io.sailex.ai.npc.client.model.database.Action
import io.sailex.ai.npc.client.model.database.Conversation
import io.sailex.ai.npc.client.model.database.Requirement
import io.sailex.ai.npc.client.model.database.Template

data class Resources(
    val actions: List<Action>,
    val requirements: List<Requirement>,
    val templates: List<Template>,
    val conversations: List<Conversation>
)
