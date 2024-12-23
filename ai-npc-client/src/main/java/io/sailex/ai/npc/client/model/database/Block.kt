package io.sailex.ai.npc.client.model.database

data class Block(
    val id: String,
    val name: String,
    override val embedding: DoubleArray,
    val miningLevel: String,
    val toolNeeded: String
) : Resource
