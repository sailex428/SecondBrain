package me.sailex.ai.npc.database.repositories

import me.sailex.ai.npc.database.SqliteClient
import me.sailex.ai.npc.model.database.Function
import me.sailex.ai.npc.model.database.Resource
import me.sailex.ai.npc.util.VectorUtil

class FunctionRepository(
    sqliteClient: SqliteClient,
) : ARepository(sqliteClient) {
    override fun createTable() {
        val sql = """
            CREATE TABLE IF NOT EXISTS functions (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    function_name CHAR(30) NOT NULL,
                    context_embedding BLOB NOT NULL
            );
        """
        sqliteClient.create(sql)
    }

    fun insert(
        function: Function
    ) {
        val statement =
            sqliteClient.buildPreparedStatement(
                "INSERT INTO functions (function_name, context_embedding) VALUES (?, ?)",
            )
        statement.setString(1, function.name)
        statement.setBytes(2, VectorUtil.convertToBytes(function.embedding))
        sqliteClient.insert(statement)
    }

    override fun selectAll(): List<Resource> {
        val sql = "SELECT * FROM functions"
        return executeAndProcessConversations(sql)
    }

    private fun executeAndProcessConversations(sql: String): List<Function> {
        val result = sqliteClient.select(sql)
        val functions = arrayListOf<Function>()

        while (result.next()) {
            val function =
                Function(
                    result.getString("function_name"),
                    VectorUtil.convertToDoubles(result.getBytes("context_embedding")),
                )
            functions.add(function)
        }
        result.close()
        return functions
    }
}