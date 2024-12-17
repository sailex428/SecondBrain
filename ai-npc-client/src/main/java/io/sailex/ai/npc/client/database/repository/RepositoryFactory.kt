package io.sailex.ai.npc.client.database.repository

import io.sailex.ai.npc.client.database.SqliteClient
import io.sailex.ai.npc.client.llm.ILLMClient

class RepositoryFactory(llmClient: ILLMClient) {

    val sqliteClient = SqliteClient()
    val requirementsRepository = RequirementsRepository(sqliteClient, llmClient)
    val actionsRepository = ActionsRepository(sqliteClient, requirementsRepository, llmClient)
    val templatesRepository = TemplatesRepository(sqliteClient, llmClient)
    val conversationRepository = ConversationRepository(sqliteClient, llmClient)
    val repositories = listOf<IRepository>(
        requirementsRepository, actionsRepository,
        templatesRepository, conversationRepository)

    fun initRepositories() {
        sqliteClient.initDatabase("actions");
        requirementsRepository.init()
        actionsRepository.init()
        templatesRepository.init()
        conversationRepository.init()
    }

}