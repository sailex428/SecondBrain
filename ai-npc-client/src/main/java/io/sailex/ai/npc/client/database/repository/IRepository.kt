package io.sailex.ai.npc.client.database.repository

import io.sailex.ai.npc.client.model.database.Resource

interface IRepository {

    fun init()
    fun createTable()
    fun getMostRelevantResources(prompt: String): List<Resource>
    fun selectAll(): List<Resource>

}