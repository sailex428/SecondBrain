package io.sailex.ai.npc.client.model.database

data class RequirementResource(
    val type: String,
    val name: String,
    override val embedding: DoubleArray,
    val tableNeeded: String,
    val itemsNeeded: Map<String, Int>
) : Resource
