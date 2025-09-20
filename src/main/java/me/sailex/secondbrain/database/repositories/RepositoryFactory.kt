package me.sailex.secondbrain.database.repositories

import me.sailex.secondbrain.SecondBrain
import me.sailex.secondbrain.database.SqliteClient
import me.sailex.secondbrain.util.LogUtil
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors

class RepositoryFactory(
    val sqliteClient: SqliteClient
) {
    private val executor = Executors.newSingleThreadExecutor()
    val conversationRepository = ConversationRepository(sqliteClient)

    fun initRepositories() {
        CompletableFuture.runAsync({
            sqliteClient.initDatabase(SecondBrain.MOD_ID)
            conversationRepository.init()
        }, executor).exceptionally {
            LogUtil.error("Failed to init sql database", it)
            null
        }
        executor.shutdown()
    }
}
