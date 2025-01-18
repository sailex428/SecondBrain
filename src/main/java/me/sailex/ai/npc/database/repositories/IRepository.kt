package me.sailex.ai.npc.database.repositories

import me.sailex.ai.npc.model.database.Resource

interface IRepository {
    fun init()

    fun createTable()

    fun getMostRelevantResources(promptEmbedding: DoubleArray): List<Resource>

    fun selectAll(): List<Resource>
}
