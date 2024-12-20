package io.sailex.ai.npc.client.database.repositories

import io.sailex.ai.npc.client.model.database.Resource
import io.sailex.ai.npc.client.util.VectorUtil.cosineSimilarity
import io.sailex.ai.npc.client.util.VectorUtil.isSimilar

abstract class ARepository() : IRepository {

    override fun init() {
        createTable()
    }

    override fun getMostRelevantResources(promptEmbedding: DoubleArray): List<Resource> {
        return selectAll().map { resource -> Pair(resource, cosineSimilarity(promptEmbedding, resource.embedding)) }
            .filter { resource -> isSimilar(resource.second) }
            .sortedByDescending { it.second }
            .take(3)
            .map { it.first }
    }
}