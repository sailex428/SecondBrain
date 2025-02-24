package me.sailex.ai.npc.util

import me.sailex.ai.npc.llm.ILLMClient
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

}