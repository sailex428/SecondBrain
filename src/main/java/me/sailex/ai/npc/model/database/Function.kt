package me.sailex.ai.npc.model.database

data class Function(
    val name: String,
    override val embedding: DoubleArray
): Resource