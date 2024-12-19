package io.sailex.ai.npc.client.database.repository

import io.sailex.ai.npc.client.database.SqliteClient
import io.sailex.ai.npc.client.llm.ILLMClient
import io.sailex.ai.npc.client.model.database.Action
import io.sailex.ai.npc.client.model.database.Requirement
import io.sailex.ai.npc.client.model.database.Resource
import io.sailex.ai.npc.client.util.VectorUtil

class ActionsRepository(
    val sqliteClient: SqliteClient,
    val requirementsRepository: RequirementsRepository,
    llmClient: ILLMClient
) : ARepository(llmClient) {

    override fun createTable() {
        val sql = """
            CREATE TABLE IF NOT EXISTS actions (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    description TEXT,
                    description_embedding BLOB,
                    example TEXT NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    requirements TEXT
            );
        """
        sqliteClient.create(sql)
    }

    fun insert(name: String, description: String, descriptionEmbedding: DoubleArray, example: String, requirements: List<Int>) {
        val statement = sqliteClient.buildPreparedStatement("INSERT INTO actions (name, description, description_embedding, example, requirements) VALUES (?, ?, ?, ?, ?)")
        statement.setString(1, name)
        statement.setString(2, description)
        statement.setBytes(3, VectorUtil.convertToBytes(descriptionEmbedding))
        statement.setString(4, example)
        statement.setString(5, requirements.joinToString(","))
        sqliteClient.insert(statement)
    }

    override fun selectAll(): List<Resource> {
        val sql = "SELECT * FROM actions"
        val result = sqliteClient.select(sql)
        val actions = arrayListOf<Action>()

        while(result.next()) {
            val requirements = result.getString("requirements").split(",").map { it.toInt() }
            val action = Action(
                result.getInt("id"),
                result.getString("name"),
                result.getString("description"),
                VectorUtil.convertToDoubles(result.getBytes("description_embedding")),
                result.getString("example"),
                requirementsRepository.select(requirements) as List<Requirement>,
                result.getString("created_at")
            )
            actions.add(action)
        }
        return actions
    }

}