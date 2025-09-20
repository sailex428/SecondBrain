package me.sailex.secondbrain.database.repositories

import me.sailex.secondbrain.database.SqliteClient
import me.sailex.secondbrain.model.database.Conversation
import java.util.UUID

class ConversationRepository(
    val sqliteClient: SqliteClient,
) {
    fun init() {
        createTable()
    }

    fun createTable() {
        val sql = """
            CREATE TABLE IF NOT EXISTS conversations (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                uuid CHARACTER(36) NOT NULL,
                role CHARACTER(9) NOT NULL,
                message TEXT NOT NULL
            );
        """
        sqliteClient.create(sql)
    }

    fun insert(conversation: Conversation) {
        val statement =
            sqliteClient.buildPreparedStatement(
                "INSERT INTO conversations (uuid, role, message, timestamp) VALUES (?, ?, ?, ?)",
            )
        statement.setString(1, conversation.uuid.toString())
        statement.setString(2, conversation.role)
        statement.setString(3, conversation.message)
        sqliteClient.insert(statement)
    }

    /**
     * Selects latest one hundred conversations of an NPC
     */
    fun selectByUuid(uuid: UUID): List<Conversation> {
        val sql = "SELECT * FROM conversations WHERE uuid = '%s' ORDER BY timestamp DESC LIMIT 100".format(uuid.toString())
        return executeAndProcessConversations(sql)
    }


    private fun executeAndProcessConversations(sql: String): List<Conversation> {
        val result = sqliteClient.select(sql)
        val conversations = arrayListOf<Conversation>()

        while (result.next()) {
            val conversation =
                Conversation(
                    UUID.fromString(result.getString("uuid")),
                    result.getString("role"),
                    result.getString("message")
                )
            conversations.add(conversation)
        }
        result.close()
        return conversations
    }
}
