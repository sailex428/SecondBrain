package me.sailex.secondbrain.model.database

data class Recipe(
    val name: String,
    val type: String,
    val tableNeeded: String,
    val itemsNeeded: String,
    override val embedding: DoubleArray,
) : Resource
