package io.sailex.ai.npc.client.database.repository

import io.sailex.ai.npc.client.llm.ILLMClient
import io.sailex.ai.npc.client.model.database.Resource
import io.sailex.ai.npc.client.util.VectorUtil.cosineSimilarity
import io.sailex.ai.npc.client.util.VectorUtil.isSimilar

abstract class ARepository(val llmClient: ILLMClient) : IRepository {

    override fun init() {
        createTable()
    }

    override fun getMostRelevantResources(prompt: String): List<Resource> {
        val promptEmbedding = llmClient.generateEmbedding(listOf(prompt))
        return selectAll().map { resource -> Pair(resource, cosineSimilarity(promptEmbedding, resource.embedding)) }
            .filter { resource -> isSimilar(resource.second) }
            .sortedByDescending { it.second }
            .take(3)
            .map { it.first }
    }
}