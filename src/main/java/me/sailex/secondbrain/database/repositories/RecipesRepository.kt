package me.sailex.secondbrain.database.repositories

import me.sailex.secondbrain.database.SqliteClient
import me.sailex.secondbrain.model.database.Recipe
import me.sailex.secondbrain.model.database.Resource
import me.sailex.secondbrain.util.VectorUtil
import java.sql.ResultSet

class RecipesRepository(
    sqliteClient: SqliteClient,
) : ARepository(sqliteClient) {
    override fun createTable() {
        val sql = """
            CREATE TABLE IF NOT EXISTS recipes (
                    name CHARACTER(255) UNIQUE PRIMARY KEY,
                    type CHARACTER(16) NOT NULL,
                    table_needed CHARACTER(14) NOT NULL,
                    items_needed VARCHAR(510) NOT NULL,
                    name_embedding BLOB
            );
        """
        sqliteClient.create(sql)
    }

    fun insert(
        recipe: Recipe
    ) {
        val statement =
            sqliteClient.buildPreparedStatement(
                "INSERT INTO recipes (name, type, table_needed, items_needed, name_embedding) VALUES (?, ?, ?, ?, ?)" +
                    " ON CONFLICT(name) DO UPDATE SET items_needed = excluded.items_needed, name_embedding = excluded.name_embedding",
            )
        statement.setString(1, recipe.name)
        statement.setString(2, recipe.type)
        statement.setString(3, recipe.tableNeeded)
        statement.setString(4, recipe.itemsNeeded)
        statement.setBytes(5, VectorUtil.convertToBytes(recipe.embedding))
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
                    result.getString("name"),
                    result.getString("type"),
                    result.getString("table_needed"),
                    result.getString("items_needed"),
                    VectorUtil.convertToDoubles(result.getBytes("name_embedding")),
                )
            recipes.add(requirement)
        }
        result.close()
        return recipes
    }
}
