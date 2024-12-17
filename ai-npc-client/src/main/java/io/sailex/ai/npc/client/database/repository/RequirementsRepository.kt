package io.sailex.ai.npc.client.database.repository

import io.sailex.ai.npc.client.database.SqliteClient
import io.sailex.ai.npc.client.llm.ILLMClient
import io.sailex.ai.npc.client.model.database.Requirement
import io.sailex.ai.npc.client.model.database.Resource
import io.sailex.ai.npc.client.util.VectorUtil
import java.sql.ResultSet

class RequirementsRepository(
    val sqliteClient: SqliteClient,
    llmClient: ILLMClient
) : ARepository(llmClient) {

    override fun createTable() {
        val sql = """
            CREATE TABLE IF NOT EXISTS requirements (
                    id INTEGER PRIMARY KEY,
                    name TEXT NOT NULL,
                    name_embedding BLOB,
                    crafting_table_needed BOOLEAN NOT NULL,
                    blocks_needed TEXT NOT NULL
            );
        """
        sqliteClient.executeQuery(sql)
    }

    fun insert(name: String, nameEmbedding: DoubleArray, craftingTableNeeded: Boolean, blocksNeeded: String) {
        val sql = "INSERT INTO requirements (name, name_embedding, crafting_table_needed, blocks_needed) VALUES (%S, %S, %S, %S)"
        sqliteClient.executeQuery(String.format(sql, name, nameEmbedding, craftingTableNeeded, blocksNeeded))
    }

    fun select(requirementIds: List<Int>): List<Resource> {
        val sql = "SELECT * FROM requirements WHERE id IN (%S)"
        val result = sqliteClient.query(String.format(sql, requirementIds.joinToString(",")))
        return processResult(result)
    }

    override fun selectAll(): List<Resource> {
        val sql = "SELECT * FROM requirements"
        val result = sqliteClient.query(sql)
        return processResult(result)
    }

    private fun processResult(result: ResultSet): List<Resource> {
        val requirements = arrayListOf<Requirement>()
        while(result.next()) {
            val requirement = Requirement(
                result.getInt("id"),
                result.getString("name"),
                VectorUtil.convertToDoubles(result.getBytes("name_embedding")),
                result.getBoolean("crafting_table_needed"),
                parseBlocksNeededToMap(result.getString("blocks_needed"))
            )
            requirements.add(requirement)
        }
        return requirements
    }

    private fun parseBlocksNeededToMap(blocksNeeded: String): Map<String, Int> {
        return blocksNeeded.split(",")
            .map { it.split("=") }
            .associate { it[0] to it[1].toInt() }
    }

}