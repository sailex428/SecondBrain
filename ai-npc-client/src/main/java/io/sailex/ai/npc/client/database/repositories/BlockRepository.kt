package io.sailex.ai.npc.client.database.repositories

import io.sailex.ai.npc.client.database.SqliteClient
import io.sailex.ai.npc.client.model.database.Block
import io.sailex.ai.npc.client.model.database.Resource
import io.sailex.ai.npc.client.util.VectorUtil

class BlockRepository(
    val sqliteClient: SqliteClient,
) : ARepository() {
    override fun createTable() {
        val sql = """
            CREATE TABLE IF NOT EXISTS blocks (
                    id TEXT UNIQUE NOT NULL,
                    name TEXT NOT NULL,
                    name_embedding BLOB
            );
        """
        sqliteClient.create(sql)
    }

    fun insert(
        id: String,
        name: String,
        nameEmbedding: DoubleArray,
    ) {
        val statement =
            sqliteClient.buildPreparedStatement(
                "INSERT INTO blocks (id, name, name_embedding) VALUES (?, ?, ?) " +
                        "ON CONFLICT(id) DO UPDATE SET name_embedding = excluded.name_embedding",
            )
        statement.setString(1, id)
        statement.setString(2, name)
        statement.setBytes(3, VectorUtil.convertToBytes(nameEmbedding))
        sqliteClient.insert(statement)
    }

    override fun selectAll(): List<Resource> {
        val sql = "SELECT * FROM blocks"
        val result = sqliteClient.select(sql)
        val blocks = arrayListOf<Block>()

        while (result.next()) {
            val block =
                Block(
                    result.getString("id"),
                    result.getString("name"),
                    VectorUtil.convertToDoubles(result.getBytes("name_embedding"))
                )
            blocks.add(block)
        }
        return blocks
    }
}
