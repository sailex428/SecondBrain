package me.sailex.ai.npc.database.repositories

import me.sailex.ai.npc.database.SqliteClient
import me.sailex.ai.npc.llm.ILLMClient
import me.sailex.ai.npc.model.database.SkillResource
import me.sailex.ai.npc.model.database.Block
import me.sailex.ai.npc.model.database.Conversation
import me.sailex.ai.npc.model.database.Recipe
import me.sailex.ai.npc.model.database.Resources
import java.util.concurrent.Executors

class RepositoryFactory(
    val llmClient: ILLMClient,
    val sqliteClient: SqliteClient
) {
    private val executor = Executors.newSingleThreadExecutor()

    val recipesRepository = RecipesRepository(sqliteClient)
    val skillRepository = SkillRepository(sqliteClient)
    val conversationRepository = ConversationRepository(sqliteClient)
    val blockRepository = BlockRepository(sqliteClient)

    fun initRepositories() {
        executor.execute {
            sqliteClient.initDatabase("skills")
            recipesRepository.init()
            skillRepository.init()
            conversationRepository.init()
            blockRepository.init()
        }
    }

    fun getRelevantResources(prompt: String): Resources {
        val promptEmbedding = llmClient.generateEmbedding(listOf(prompt))
        return Resources(
            skillRepository.getMostRelevantResources(promptEmbedding).filterIsInstance<SkillResource>(),
            recipesRepository.getMostRelevantResources(promptEmbedding).filterIsInstance<Recipe>(),
            conversationRepository.getMostRelevantResources(promptEmbedding).filterIsInstance<Conversation>(),
            blockRepository.getMostRelevantResources(promptEmbedding).filterIsInstance<Block>(),
        )
    }
}
