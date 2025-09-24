package me.sailex.secondbrain.model.database

import java.util.UUID

data class Conversation(
    val uuid: UUID,
    val role: String,
    val message: String
)
