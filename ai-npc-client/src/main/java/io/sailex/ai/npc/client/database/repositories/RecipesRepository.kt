package io.sailex.ai.npc.client.database.repositories

import io.sailex.ai.npc.client.database.SqliteClient
import io.sailex.ai.npc.client.model.database.Recipe
import io.sailex.ai.npc.client.model.database.Resource
import io.sailex.ai.npc.client.util.VectorUtil
import java.sql.ResultSet

class RecipesRepository(
    val sqliteClient: SqliteClient,
) : ARepository() {
    override fun createTable() {
        val sql = """
            CREATE TABLE IF NOT EXISTS recipes (
                    type TEXT NOT NULL,
                    name TEXT UNIQUE NOT NULL,
                    name_embedding BLOB,
                    table_needed BOOLEAN NOT NULL,
                    items_needed TEXT
            );
        """
        sqliteClient.create(sql)
    }

    fun insert(
        type: String,
        name: String,
        nameEmbedding: DoubleArray,
        tableNeeded: String,
        itemsNeeded: String,
    ) {
        val statement =
            sqliteClient.buildPreparedStatement(
                "INSERT INTO recipes (type, name, name_embedding, table_needed, items_needed) VALUES (?, ?, ?, ?, ?)" +
                    " ON CONFLICT(name) DO UPDATE SET blocks_needed = excluded.items_needed",
            )
        statement.setString(1, type)
        statement.setString(2, name)
        statement.setBytes(3, VectorUtil.convertToBytes(nameEmbedding))
        statement.setString(4, tableNeeded)
        statement.setString(5, itemsNeeded)
        sqliteClient.insert(statement)
    }

    fun select(requirementIds: List<Int>): List<Resource> {
        val sql = "SELECT * FROM recipes WHERE id IN (%S)"
        val result = sqliteClient.select(String.format(sql, requirementIds.joinToString(",")))
        return processResult(result)
    }

    fun select(type: String): List<Resource> {
        val sql = "SELECT * FROM recipes WHERE type IN (%S)"
        val result = sqliteClient.select(String.format(sql, type))
        return processResult(result)
    }

    override fun selectAll(): List<Resource> {
        val sql = "SELECT * FROM recipes"
        val result = sqliteClient.select(sql)
        return processResult(result)
    }

    private fun processResult(result: ResultSet): List<Resource> {
        val recipes = arrayListOf<Recipe>()
        while (result.next()) {
            val requirement =
                Recipe(
                    result.getString("type"),
                    result.getString("name"),
                    VectorUtil.convertToDoubles(result.getBytes("name_embedding")),
                    result.getString("table_needed"),
                    parseItemsNeededToMap(result.getString("items_needed")),
                )
            recipes.add(requirement)
        }
        return recipes
    }

    private fun parseItemsNeededToMap(itemsNeeded: String): Map<String, Int> =
        itemsNeeded
            .split(",")
            .map { it.split("=") }
            .associate { it[0] to it[1].toInt() }
}
