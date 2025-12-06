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
        sqliteClient.update(sql)
    }

    fun insert(conversation: Conversation) {
        val statement =
            sqliteClient.buildPreparedStatement(
                "INSERT INTO conversations (uuid, role, message) VALUES (?, ?, ?)",
            )
        statement.setString(1, conversation.uuid.toString())
        statement.setString(2, conversation.role)
        statement.setString(3, conversation.message)
        sqliteClient.update(statement)
    }

    /**
     * Selects latest one hundred conversations of an NPC
     */
    fun selectByUuid(uuid: UUID): List<Conversation> {
        val sql = "SELECT * FROM conversations WHERE uuid = '%s' ORDER BY id DESC LIMIT 100".format(uuid.toString())
        return executeAndProcessConversations(sql)
    }

    /**
     * Deletes all conversations of the given uuid.
     */
    fun deleteByUuid(uuid: UUID) {
        val sql = "DELETE FROM conversations WHERE uuid = '%s'".format(uuid.toString())
        sqliteClient.update(sql)
    }

    private fun executeAndProcessConversations(sql: String): List<Conversation> {
        val result = sqliteClient.query(sql)
        val conversations = arrayListOf<Conversation>()

        if (result == null) return emptyList()

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
