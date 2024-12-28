package io.sailex.ai.npc.client.database.repositories

import io.sailex.ai.npc.client.database.SqliteClient
import io.sailex.ai.npc.client.model.database.SkillResource
import io.sailex.ai.npc.client.model.database.Resource
import io.sailex.ai.npc.client.util.VectorUtil

class SkillRepository(
    sqliteClient: SqliteClient,
) : ARepository(sqliteClient) {
    override fun createTable() {
        val sql = """
            CREATE TABLE IF NOT EXISTS skills (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    example TEXT NOT NULL,
                    example_embedding BLOB,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            );
        """
        sqliteClient.create(sql)
    }

    fun insert(
        name: String,
        example: String,
        exampleEmbedding: DoubleArray,
    ) {
        val statement =
            sqliteClient.buildPreparedStatement(
                "INSERT INTO skills (name, example, example_embedding) VALUES (?, ?, ?)",
            )
        statement.setString(1, name)
        statement.setString(2, example)
        statement.setBytes(3, VectorUtil.convertToBytes(exampleEmbedding))
        sqliteClient.insert(statement)
    }

    fun selectCount(): Int {
        return super.selectCount("skills")
    }

    override fun selectAll(): List<Resource> {
        val sql = "SELECT * FROM skills"
        val result = sqliteClient.select(sql)
        val skillResources = arrayListOf<SkillResource>()

        while (result.next()) {
            val skillResource =
                SkillResource(
                    result.getInt("id"),
                    result.getString("name"),
                    result.getString("example"),
                    VectorUtil.convertToDoubles(result.getBytes("example_embedding")),
                    result.getString("created_at")
                )
            skillResources.add(skillResource)
        }
        result.close()
        return skillResources
    }
}
