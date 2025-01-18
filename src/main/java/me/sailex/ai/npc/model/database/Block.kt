package me.sailex.ai.npc.model.database

data class Block(
    val id: String,
    val name: String,
    override val embedding: DoubleArray
) : Resource
