package io.sailex.ai.npc.client.database.repositories

import io.sailex.ai.npc.client.database.SqliteClient
import io.sailex.ai.npc.client.llm.ILLMClient
import io.sailex.ai.npc.client.model.database.Template
import io.sailex.ai.npc.client.util.VectorUtil

class TemplatesRepository(
    val sqliteClient: SqliteClient,
    llmClient: ILLMClient
) : ARepository(llmClient) {

    override fun createTable() {
        val query = """
           CREATE TABLE IF NOT EXISTS templates (
                name TEXT UNIQUE NOT NULL,
                action TEXT NOT NULL,
                action_embedded BLOB
           );
        """
        sqliteClient.create(query)
    }

    fun insert(name: String, action: String, actionEmbedded: DoubleArray) {
        val statement = sqliteClient.buildPreparedStatement("INSERT INTO templates (name, action, action_embedded) VALUES (?, ?, ?)" +
                " ON CONFLICT(name) DO UPDATE SET action = excluded.action, action_embedded = excluded.action_embedded")
        statement.setString(1, name)
        statement.setString(2, action)
        statement.setBytes(3, VectorUtil.convertToBytes(actionEmbedded))
        sqliteClient.insert(statement)
    }

    override fun selectAll(): List<Template> {
        val query = "SELECT * FROM templates"
        val result = sqliteClient.select(query)
        val templates = arrayListOf<Template>()

        while(result.next()) {
            val template = Template(
                result.getString("name"),
                result.getString("action"),
                VectorUtil.convertToDoubles(result.getBytes("action_embedded"))
            )
            templates.add(template)
        }
        return templates;
    }

}
