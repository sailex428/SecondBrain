package me.sailex.ai.npc.model.database

data class Recipe(
    val type: String,
    val name: String,
    override val embedding: DoubleArray,
    val tableNeeded: String,
    val itemsNeeded: Map<String, Int>,
) : Resource
