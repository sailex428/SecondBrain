package me.sailex.secondbrain.database.repositories

import me.sailex.secondbrain.database.SqliteClient
import me.sailex.secondbrain.model.database.Conversation
import me.sailex.secondbrain.model.database.Resource
import me.sailex.secondbrain.util.VectorUtil

class ConversationRepository(
    sqliteClient: SqliteClient,
) : ARepository(sqliteClient) {
    override fun createTable() {
        val sql = """
            CREATE TABLE IF NOT EXISTS conversations (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    npc_name CHARACTER(16) NOT NULL,
                    conversation TEXT NOT NULL,
                    timestamp DATETIME,
                    conversation_embedding BLOB
            );
        """
        sqliteClient.create(sql)
    }

    fun insert(
        conversation: Conversation
    ) {
        val statement =
            sqliteClient.buildPreparedStatement(
                "INSERT INTO conversations (npc_name, conversation, timestamp, conversation_embedding) VALUES (?, ?, ?, ?)",
            )
        statement.setString(1, conversation.npcName)
        statement.setString(2, conversation.message)
        statement.setTimestamp(3, conversation.timestamp)
        statement.setBytes(4, VectorUtil.convertToBytes(conversation.embedding))
        sqliteClient.insert(statement)
    }

    /**
     * Selects latest two hundred conversations of a npc
     */
    fun selectByName(npcName: String): List<Conversation> {
        val sql = "SELECT * FROM conversations WHERE npc_name = '%s' ORDER BY timestamp DESC LIMIT 200".format(npcName)
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
                    result.getString("npc_name"),
                    result.getString("conversation"),
                    result.getTimestamp("timestamp"),
                    VectorUtil.convertToDoubles(result.getBytes("conversation_embedding")),
                )
            conversations.add(conversation)
        }
        result.close()
        return conversations
    }
}
