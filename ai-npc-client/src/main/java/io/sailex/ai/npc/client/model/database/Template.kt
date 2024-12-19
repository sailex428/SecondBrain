package io.sailex.ai.npc.client.model.database

data class Template(
    val name: String,
    val action: String,
    override val embedding: DoubleArray,
) : Resource
