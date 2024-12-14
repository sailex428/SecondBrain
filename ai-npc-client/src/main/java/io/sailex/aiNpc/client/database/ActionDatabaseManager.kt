package io.sailex.aiNpc.client.database

import io.sailex.aiNpc.client.model.db.Action
import io.sailex.aiNpc.client.util.VectorUtil

class ActionDatabaseManager(val sqliteClient: SqliteClient) {

    fun init() {
        createActionTable()
        createRequirementsTable()
    }

    /**
     * Creates actions table in database
     */
    fun createActionTable() {
        val sql = """
            CREATE TABLE IF NOT EXISTS actions (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        name TEXT NOT NULL,
                        name_embedding BLOB,
                        description TEXT,
                        description_embedding BLOB,
                        example TEXT,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                    )
        """
        sqliteClient.create(sql)
    }

    fun createRequirementsTable() {
        val sql = """
            CREATE TABLE IF NOT EXISTS requirements (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        action_id INTEGER,
                        requirement TEXT,
                        requirement_embedding BLOB,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                    )
        """
        sqliteClient.create(sql)
    }

    /**
     * Add an action to the database.
     */
    fun insertActions(actions: List<Action>) {
        val sql = "INSERT INTO actions (name, name_embedding, description, description_embedding, example) VALUES (%S, %S, %S, %S, %S)"
        actions.forEach { action ->
            sqliteClient.insert(String.format(sql, action.name(), action.name_embedding(),
                action.description(), action.description_embedding(), action.example()))
        }
    }

    /**
     * Select all minecraft actions from the database.
     *
     * @return a list of actions
     */
    fun selectActions(): List<Action> {
        val sql = "SELECT * FROM actions"
        val result = sqliteClient.select(sql)
        val actions = arrayListOf<Action>()

        while(result.next()) {
            val action = Action(
                result.getInt("id"),
                result.getString("name"),
                VectorUtil.convertToFloats(result.getBytes("name_embedding")),
                result.getString("description"),
                VectorUtil.convertToFloats(result.getBytes("description_embedding")),
                result.getString("example")
            )
            actions.add(action)
        }
        return actions
    }
}