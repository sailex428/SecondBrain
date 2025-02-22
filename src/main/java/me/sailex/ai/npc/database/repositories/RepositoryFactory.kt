package me.sailex.ai.npc.database.repositories

import me.sailex.ai.npc.SecondBrain
import me.sailex.ai.npc.database.SqliteClient
import me.sailex.ai.npc.util.LogUtil
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors

class RepositoryFactory(
    val sqliteClient: SqliteClient
) {
    private val executor = Executors.newSingleThreadExecutor()
    val conversationRepository = ConversationRepository(sqliteClient)
    val recipesRepository = RecipesRepository(sqliteClient)
    val functionRepository = FunctionRepository(sqliteClient)

    fun initRepositories() {
        CompletableFuture.runAsync({
            sqliteClient.initDatabase(SecondBrain.MOD_ID)
            conversationRepository.init()
            recipesRepository.init()
            functionRepository.init()
        }, executor).exceptionally( {
            LogUtil.error("Failed to init sql database", true)
            return@exceptionally null
        } )
        executor.shutdown()
    }
}
