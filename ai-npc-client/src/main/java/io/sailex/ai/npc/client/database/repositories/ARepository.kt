package io.sailex.ai.npc.client.database.repositories

import io.sailex.ai.npc.client.database.SqliteClient
import io.sailex.ai.npc.client.model.database.Resource
import io.sailex.ai.npc.client.util.VectorUtil.cosineSimilarity
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
