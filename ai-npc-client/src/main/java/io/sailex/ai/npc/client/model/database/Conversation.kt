package io.sailex.ai.npc.client.model.database

data class Conversation(
    override val id: Int,
    val npcName: String,
    val message: String,
    override val embedding: DoubleArray,
    val timeStamp: String
) : Resource