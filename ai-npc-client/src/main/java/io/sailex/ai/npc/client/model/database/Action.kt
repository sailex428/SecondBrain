package io.sailex.ai.npc.client.model.database

data class Action(
    override val id: Int,
    val name: String,
    val description: String,
    override val embedding: DoubleArray,
    val example: String,
    val requirements: List<Requirement>,
    val timeStamp: String,
) : Resource
