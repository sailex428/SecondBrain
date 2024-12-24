package io.sailex.ai.npc.client.model.database

data class SkillResource(
    val id: Int,
    val name: String,
    val example: String,
    override val embedding: DoubleArray,
    val timeStamp: String,
) : Resource
