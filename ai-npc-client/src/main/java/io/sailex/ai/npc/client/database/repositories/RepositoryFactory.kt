package io.sailex.ai.npc.client.database.repositories

import io.sailex.ai.npc.client.database.SqliteClient
import io.sailex.ai.npc.client.llm.ILLMClient
import io.sailex.ai.npc.client.model.database.ActionResource
import io.sailex.ai.npc.client.model.database.Conversation
import io.sailex.ai.npc.client.model.database.Requirement
import io.sailex.ai.npc.client.model.database.Template
import io.sailex.ai.npc.client.model.interaction.Resources

class RepositoryFactory(val llmClient: ILLMClient) {

    val sqliteClient = SqliteClient()
    val requirementsRepository = RequirementsRepository(sqliteClient)
    val actionsRepository = ActionsRepository(sqliteClient, requirementsRepository)
    val templatesRepository = TemplatesRepository(sqliteClient)
    val conversationRepository = ConversationRepository(sqliteClient)

    fun initRepositories() {
        sqliteClient.initDatabase("actions")
        requirementsRepository.init()
        actionsRepository.init()
        templatesRepository.init()
        conversationRepository.init()
    }

    fun getRelevantResources(prompt: String): Resources {
        val promptEmbedding = llmClient.generateEmbedding(listOf(prompt))
        return Resources(
            actionsRepository.getMostRelevantResources(promptEmbedding).filterIsInstance<ActionResource>(),
            requirementsRepository.getMostRelevantResources(promptEmbedding).filterIsInstance<Requirement>(),
            templatesRepository.getMostRelevantResources(promptEmbedding).filterIsInstance<Template>(),
            conversationRepository.getMostRelevantResources(promptEmbedding).filterIsInstance<Conversation>())
    }

}