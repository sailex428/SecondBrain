package io.sailex.ai.npc.client.database.repository

import io.sailex.ai.npc.client.database.SqliteClient
import io.sailex.ai.npc.client.llm.ILLMClient
import io.sailex.ai.npc.client.model.database.Conversation
import io.sailex.ai.npc.client.model.database.Resource
import io.sailex.ai.npc.client.util.VectorUtil

class ConversationRepository(
    val sqliteClient: SqliteClient,
    llmClient: ILLMClient
) : ARepository(llmClient) {

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
        sqliteClient.executeQuery(sql)
    }

    fun insert(npcName: String, conversation: String, conversationEmbedding: DoubleArray) {
        val sql = "INSERT INTO conversations (npc_name, conversation, conversation_embedding) VALUES (%S, %S, %S)"
        sqliteClient.executeQuery(String.format(sql, npcName, conversation, VectorUtil.convertToBytes(conversationEmbedding)))
    }

    override fun selectAll(): List<Resource> {
        val sql = "SELECT * FROM conversations"
        val result = sqliteClient.query(sql)
        val conversations = arrayListOf<Conversation>()

        while(result.next()) {
            val conversation = Conversation(
                result.getInt("id"),
                result.getString("npc_name"),
                result.getString("conversation"),
                VectorUtil.convertToDoubles(result.getBytes("conversation_embedding")),
                result.getString("created_at")
            )
            conversations.add(conversation)
        }
        return conversations
    }

}