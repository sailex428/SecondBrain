package io.sailex.ai.npc.client.database.repositories

import io.sailex.ai.npc.client.database.SqliteClient
import io.sailex.ai.npc.client.llm.ILLMClient
import io.sailex.ai.npc.client.model.database.ActionResource
import io.sailex.ai.npc.client.model.database.Block
import io.sailex.ai.npc.client.model.database.Conversation
import io.sailex.ai.npc.client.model.database.Recipe
import io.sailex.ai.npc.client.model.database.Resources

class RepositoryFactory(
    val llmClient: ILLMClient,
) {
    val sqliteClient = SqliteClient()
    val recipesRepository = RecipesRepository(sqliteClient)
    val actionsRepository = ActionsRepository(sqliteClient)
    val conversationRepository = ConversationRepository(sqliteClient)
    val blockRepository = BlockRepository(sqliteClient)

    fun initRepositories() {
        sqliteClient.initDatabase("actions")
        recipesRepository.init()
        actionsRepository.init()
        conversationRepository.init()
        blockRepository.init()
    }

    fun getRelevantResources(prompt: String): Resources {
        val promptEmbedding = llmClient.generateEmbedding(listOf(prompt))
        return Resources(
            actionsRepository.getMostRelevantResources(promptEmbedding).filterIsInstance<ActionResource>(),
            recipesRepository.getMostRelevantResources(promptEmbedding).filterIsInstance<Recipe>(),
            conversationRepository.getMostRelevantResources(promptEmbedding).filterIsInstance<Conversation>(),
            blockRepository.getMostRelevantResources(promptEmbedding).filterIsInstance<Block>(),
        )
    }
}
