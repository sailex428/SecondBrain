package io.sailex.ai.npc.client.database.repositories

import io.sailex.ai.npc.client.database.SqliteClient
import io.sailex.ai.npc.client.model.database.RequirementResource
import io.sailex.ai.npc.client.model.database.Resource
import io.sailex.ai.npc.client.util.VectorUtil
import java.sql.ResultSet

class RequirementsRepository(val sqliteClient: SqliteClient, ) : ARepository() {

    override fun createTable() {
        val sql = """
            CREATE TABLE IF NOT EXISTS requirements (
                    type TEXT NOT NULL,
                    name TEXT UNIQUE NOT NULL,
                    name_embedding BLOB,
                    table_needed BOOLEAN NOT NULL,
                    items_needed TEXT
            );
        """
        sqliteClient.create(sql)
    }

    fun insert(type: String, name: String, nameEmbedding: DoubleArray, tableNeeded: String, itemsNeeded: String) {
        val statement =
            sqliteClient.buildPreparedStatement("INSERT INTO requirements (type, name, name_embedding, table_needed, items_needed) VALUES (?, ?, ?, ?, ?)" +
                    " ON CONFLICT(name) DO UPDATE SET blocks_needed = excluded.items_needed")
        statement.setString(1, type)
        statement.setString(2, name)
        statement.setBytes(3, VectorUtil.convertToBytes(nameEmbedding))
        statement.setString(4, tableNeeded)
        statement.setString(5, itemsNeeded)
        sqliteClient.insert(statement)
    }

    fun select(requirementIds: List<Int>): List<Resource> {
        val sql = "SELECT * FROM requirements WHERE id IN (%S)"
        val result = sqliteClient.select(String.format(sql, requirementIds.joinToString(",")))
        return processResult(result)
    }

    fun select(type: String): List<Resource> {
        val sql = "SELECT * FROM requirements WHERE type IN (%S)"
        val result = sqliteClient.select(String.format(sql, type))
        return processResult(result)
    }

    override fun selectAll(): List<Resource> {
        val sql = "SELECT * FROM requirements"
        val result = sqliteClient.select(sql)
        return processResult(result)
    }

    private fun processResult(result: ResultSet): List<Resource> {
        val requirements = arrayListOf<RequirementResource>()
        while(result.next()) {
            val requirement = RequirementResource(
                result.getString("type"),
                result.getString("name"),
                VectorUtil.convertToDoubles(result.getBytes("name_embedding")),
                result.getString("table_needed"),
                parseItemsNeededToMap(result.getString("items_needed"))
            )
            requirements.add(requirement)
        }
        return requirements
    }

    private fun parseItemsNeededToMap(itemsNeeded: String): Map<String, Int> {
        return itemsNeeded.split(",")
            .map { it.split("=") }
            .associate { it[0] to it[1].toInt() }
    }

}