package io.sailex.ai.npc.client.model.database

data class ActionResource(
    val id: Int,
    val name: String,
    val description: String,
    override val embedding: DoubleArray,
    val example: String,
    val timeStamp: String,
) : Resource
