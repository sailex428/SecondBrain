package me.sailex.secondbrain.database.repositories

import me.sailex.secondbrain.model.database.Resource

interface IRepository {
    fun init()

    fun createTable()

    fun selectAll(): List<Resource>
}
