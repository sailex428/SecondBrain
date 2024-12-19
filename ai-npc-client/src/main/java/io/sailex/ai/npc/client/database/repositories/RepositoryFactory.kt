package io.sailex.ai.npc.client.database.repositories

import io.sailex.ai.npc.client.database.SqliteClient
import io.sailex.ai.npc.client.llm.ILLMClient
import io.sailex.ai.npc.client.model.database.Action
import io.sailex.ai.npc.client.model.database.Conversation
import io.sailex.ai.npc.client.model.database.Requirement
import io.sailex.ai.npc.client.model.database.Template
import io.sailex.ai.npc.client.model.interaction.Resources

class RepositoryFactory(llmClient: ILLMClient) {

    val sqliteClient = SqliteClient()
    val requirementsRepository = RequirementsRepository(sqliteClient, llmClient)
    val actionsRepository = ActionsRepository(sqliteClient, requirementsRepository, llmClient)
    val templatesRepository = TemplatesRepository(sqliteClient, llmClient)
    val conversationRepository = ConversationRepository(sqliteClient, llmClient)

    fun initRepositories() {
        sqliteClient.initDatabase("actions")
        requirementsRepository.init()
        actionsRepository.init()
        templatesRepository.init()
        conversationRepository.init()
    }

    fun getRelevantResources(prompt: String): Resources {
        return Resources(
            actionsRepository.getMostRelevantResources(prompt) as List<Action>,
            requirementsRepository.getMostRelevantResources(prompt) as List<Requirement>,
            templatesRepository.getMostRelevantResources(prompt) as List<Template>,
            conversationRepository.getMostRelevantResources(prompt) as List<Conversation>)
    }

}