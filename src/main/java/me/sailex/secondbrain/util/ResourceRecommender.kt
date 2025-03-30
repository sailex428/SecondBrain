package me.sailex.secondbrain.util

import me.sailex.secondbrain.llm.LLMClient
import me.sailex.secondbrain.model.database.Resource
import me.sailex.secondbrain.util.VectorUtil.cosineSimilarity

object ResourceRecommender {

    @JvmStatic
    fun getRelevantResources(
        llmClient: LLMClient,
        prompt: String,
        resources: List<Resource>,
        maxTopElements: Int
    ): List<Resource> {
        val promptEmbedding = llmClient.generateEmbedding(listOf(prompt))
        return resources.map { resource -> Pair(resource, cosineSimilarity(promptEmbedding, resource.embedding)) }
            .sortedByDescending { it.second }
            .take(maxTopElements)
            .map { it.first }
    }

}
