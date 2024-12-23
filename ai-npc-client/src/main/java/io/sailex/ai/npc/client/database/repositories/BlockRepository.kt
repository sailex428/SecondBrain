package io.sailex.ai.npc.client.database.repositories

import io.sailex.ai.npc.client.database.SqliteClient
import io.sailex.ai.npc.client.model.database.Block
import io.sailex.ai.npc.client.model.database.Resource
import io.sailex.ai.npc.client.util.VectorUtil

class BlockRepository(val sqliteClient: SqliteClient) : ARepository() {

    override fun createTable() {
        val sql = """
            CREATE TABLE IF NOT EXISTS blocks (
                    id TEXT PRIMARY KEY,
                    name TEXT NOT NULL,
                    name_embedding BLOB,
                    miningLevel TEXT,
                    toolNeeded TEXT
            );
        """
        sqliteClient.create(sql)
    }

    fun insert(id: String, name: String, nameEmbedding: DoubleArray, miningLevel: String, toolNeeded: String) {
        val statement = sqliteClient.buildPreparedStatement("INSERT INTO blocks (id, name, name_embedding, miningLevel, toolNeeded) VALUES (?, ?, ?, ?)")
        statement.setString(1, id)
        statement.setString(2, name)
        statement.setBytes(3, VectorUtil.convertToBytes(nameEmbedding))
        statement.setString(4, miningLevel)
        statement.setString(5, toolNeeded)
        sqliteClient.insert(statement)
    }

    override fun selectAll(): List<Resource> {
        val sql = "SELECT * FROM blocks"
        val result = sqliteClient.select(sql)
        val blocks = arrayListOf<Block>()

        while(result.next()) {
            val block = Block(
                result.getString("id"),
                result.getString("name"),
                VectorUtil.convertToDoubles(result.getBytes("name_embedding")),
                result.getString("miningLevel"),
                result.getString("toolNeeded")
            )
            blocks.add(block)
        }
        return blocks
    }

}