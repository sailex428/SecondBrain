package io.sailex.ai.npc.client.model.database

data class Resources(
    val actionResources: List<ActionResource>,
    val requirements: List<Recipe>,
    val conversations: List<Conversation>
)