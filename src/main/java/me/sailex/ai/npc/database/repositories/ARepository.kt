package me.sailex.ai.npc.database.repositories

import me.sailex.ai.npc.database.SqliteClient
import me.sailex.ai.npc.model.database.Resource
import me.sailex.ai.npc.util.VectorUtil.cosineSimilarity
import java.sql.ResultSet

abstract class ARepository(val sqliteClient: SqliteClient) : IRepository {
    override fun init() {
        createTable()
    }

    protected fun selectCount(table: String): Int {
        val sql = "SELECT count(*) as numberOf FROM $table"
        val result: ResultSet = sqliteClient.select(sql)
        return result.getInt("numberOf")
    }

    override fun getMostRelevantResources(promptEmbedding: DoubleArray): List<Resource> =
        selectAll()
            .map { resource -> Pair(resource, cosineSimilarity(promptEmbedding, resource.embedding)) }
            .sortedByDescending { it.second }
            .take(3)
            .map { it.first }
}
