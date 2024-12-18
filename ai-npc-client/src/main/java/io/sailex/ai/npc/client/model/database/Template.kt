package io.sailex.ai.npc.client.model.database

data class Template(
    override val id: Int,
    val name: String,
    val action: String,
    override val embedding: DoubleArray,
) : Resource
