package me.sailex.ai.npc.database.repositories

import me.sailex.ai.npc.database.SqliteClient
import me.sailex.ai.npc.model.database.Recipe
import me.sailex.ai.npc.model.database.Resource
import me.sailex.ai.npc.util.VectorUtil
import java.sql.ResultSet

class RecipesRepository(
    sqliteClient: SqliteClient,
) : ARepository(sqliteClient) {
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
        recipe: Recipe
    ) {
        val statement =
            sqliteClient.buildPreparedStatement(
                "INSERT INTO recipes (type, name, name_embedding, table_needed, items_needed) VALUES (?, ?, ?, ?, ?)" +
                    " ON CONFLICT(name) DO UPDATE SET items_needed = excluded.items_needed",
            )
        statement.setString(1, recipe.type)
        statement.setString(2, recipe.name)
        statement.setBytes(3, VectorUtil.convertToBytes(recipe.embedding))
        statement.setString(4, recipe.tableNeeded)
        statement.setString(5, recipe.itemsNeeded)
        sqliteClient.insert(statement)
    }

    fun selectCount(): Int {
        return super.selectCount("recipes")
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
                    result.getString("items_needed"),
                )
            recipes.add(requirement)
        }
        result.close()
        return recipes
    }
}
