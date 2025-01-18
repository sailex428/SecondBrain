package me.sailex.ai.npc.model.database

data class Conversation(
    val id: Int,
    val npcName: String,
    val message: String,
    override val embedding: DoubleArray,
    val timeStamp: String,
) : Resource
