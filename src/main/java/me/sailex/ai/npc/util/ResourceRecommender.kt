package me.sailex.ai.npc.util

import me.sailex.ai.npc.llm.ILLMClient
import me.sailex.ai.npc.model.database.LLMFunction
import me.sailex.ai.npc.model.database.Resource
import me.sailex.ai.npc.util.VectorUtil.cosineSimilarity

object ResourceRecommender {

    @JvmStatic
    fun getRelevantResources(
        llmClient: ILLMClient,
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

    @JvmStatic
    fun getMostRelevantResources(
        llmClient: ILLMClient,
        prompt: String,
        resources: List<Resource>,
        maxTopElements: Int = 3,
        highSimilarityThreshold: Double = 0.95,
    ): List<Resource> {
        val promptEmbedding = llmClient.generateEmbedding(listOf(prompt))
        val similarities = resources.map { resource ->
            Pair(resource, cosineSimilarity(promptEmbedding, resource.embedding))
        }
        val sortedResources = similarities.sortedByDescending { it.second }
        val highlySimilarResources = sortedResources.filter { it.second >= highSimilarityThreshold }
            .map { it.first }

        return when {
            highlySimilarResources.size > maxTopElements -> highlySimilarResources.take(maxTopElements)
            highlySimilarResources.isEmpty() -> sortedResources.map { it.first }.take(maxTopElements)
            else -> highlySimilarResources
        }

    }

}
