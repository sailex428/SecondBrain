package io.sailex.ai.npc.client.database.repositories

import io.sailex.ai.npc.client.database.SqliteClient
import io.sailex.ai.npc.client.model.database.Conversation
import io.sailex.ai.npc.client.model.database.Resource
import io.sailex.ai.npc.client.util.VectorUtil

class ConversationRepository(
    sqliteClient: SqliteClient,
) : ARepository(sqliteClient) {
    override fun createTable() {
        val sql = """
            CREATE TABLE IF NOT EXISTS conversations (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    npc_name TEXT NOT NULL,
                    conversation TEXT,
                    conversation_embedding BLOB,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            );
        """
        sqliteClient.create(sql)
    }

    fun insert(
        npcName: String,
        conversation: String,
        conversationEmbedding: DoubleArray,
    ) {
        val statement =
            sqliteClient.buildPreparedStatement(
                "INSERT INTO conversations (npc_name, conversation, conversation_embedding) VALUES (?, ?, ?)",
            )
        statement.setString(1, npcName)
        statement.setString(2, conversation)
        statement.setBytes(3, VectorUtil.convertToBytes(conversationEmbedding))
        sqliteClient.insert(statement)
    }

    fun selectByName(npcName: String): List<Conversation> {
        val sql = "SELECT * FROM conversation WHERE npc_name = %S"
        return executeAndProcessConversations(sql)
    }

    override fun selectAll(): List<Resource> {
        val sql = "SELECT * FROM conversations"
        return executeAndProcessConversations(sql)
    }

    private fun executeAndProcessConversations(sql: String): List<Conversation> {
        val result = sqliteClient.select(sql)
        val conversations = arrayListOf<Conversation>()

        while (result.next()) {
            val conversation =
                Conversation(
                    result.getInt("id"),
                    result.getString("npc_name"),
                    result.getString("conversation"),
                    VectorUtil.convertToDoubles(result.getBytes("conversation_embedding")),
                    result.getString("created_at"),
                )
            conversations.add(conversation)
        }
        result.close()
        return conversations
    }
}
