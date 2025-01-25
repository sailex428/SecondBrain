package me.sailex.ai.npc.database.repositories

import me.sailex.ai.npc.database.SqliteClient
import java.sql.ResultSet

abstract class ARepository(val sqliteClient: SqliteClient) : IRepository {
    override fun init() {
        createTable()
    }

    protected fun selectCount(table: String): Int {
        val sql = "SELECT count(*) as numberOf FROM $table"
        val result: ResultSet = sqliteClient.select(sql)
        return result.getInt("numberOf")
    }
}
