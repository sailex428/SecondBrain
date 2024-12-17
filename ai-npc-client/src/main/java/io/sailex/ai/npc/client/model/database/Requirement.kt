package io.sailex.ai.npc.client.model.database

data class Requirement(
    override val id: Int,
    val name: String,
    override val embedding: DoubleArray,
    val craftingTableNeeded: Boolean,
    val blocksNeeded: Map<String, Int>
) : Resource
