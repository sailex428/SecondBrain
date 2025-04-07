package me.sailex.secondbrain.model.database

import java.sql.Timestamp

data class Conversation(
    val npcName: String,
    val message: String,
    val timestamp: Timestamp,
    override val embedding: DoubleArray
) : Resource
