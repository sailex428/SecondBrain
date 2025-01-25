package me.sailex.ai.npc.model.database

import java.sql.Timestamp

data class Conversation(
    val npcName: String,
    val message: String,
    override val embedding: DoubleArray,
    val timestamp: Timestamp,
) : Resource
