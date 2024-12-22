package io.sailex.ai.npc.client.database.repositories

import io.sailex.ai.npc.client.database.SqliteClient
import io.sailex.ai.npc.client.llm.ILLMClient
import io.sailex.ai.npc.client.model.database.ActionResource
import io.sailex.ai.npc.client.model.database.Conversation
import io.sailex.ai.npc.client.model.database.RequirementResource
import io.sailex.ai.npc.client.model.database.Resources

class RepositoryFactory(val llmClient: ILLMClient) {

    val sqliteClient = SqliteClient()
    val requirementsRepository = RequirementsRepository(sqliteClient)
    val actionsRepository = ActionsRepository(sqliteClient)
    val conversationRepository = ConversationRepository(sqliteClient)

    fun initRepositories() {
        sqliteClient.initDatabase("actions")
        requirementsRepository.init()
        actionsRepository.init()
        conversationRepository.init()
    }

    fun getRelevantResources(prompt: String): Resources {
        val promptEmbedding = llmClient.generateEmbedding(listOf(prompt))
        return Resources(
            actionsRepository.getMostRelevantResources(promptEmbedding).filterIsInstance<ActionResource>(),
            requirementsRepository.getMostRelevantResources(promptEmbedding).filterIsInstance<RequirementResource>(),
            conversationRepository.getMostRelevantResources(promptEmbedding).filterIsInstance<Conversation>()) //TODO: select records with name = npcName
    }

}