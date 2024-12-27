package io.sailex.ai.npc.client.model.database

data class Resources(
    val skillResources: List<SkillResource>,
    val requirements: List<Recipe>,
    val conversations: List<Conversation>,
    val blocks: List<Block>,
)
