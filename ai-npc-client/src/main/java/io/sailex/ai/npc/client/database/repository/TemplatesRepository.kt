package io.sailex.ai.npc.client.database.repository

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
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                action TEXT
                action_embedded
           );
        """
        sqliteClient.executeQuery(query)
    }

    fun insert(action: String, actionEmbedded: DoubleArray) {
        val query = "INSERT INTO templates (action, action_embedded) VALUES (%S, %S)"
        sqliteClient.executeQuery(String.format(query, action, VectorUtil.convertToBytes(actionEmbedded)))
    }

    override fun selectAll(): List<Template> {
        val query = "SELECT * FROM templates"
        val result = sqliteClient.query(query)
        val templates = arrayListOf<Template>()

        while(result.next()) {
            val template = Template(
                result.getInt("id"),
                result.getString("action"),
                VectorUtil.convertToDoubles(result.getBytes("action_embedded"))
            )
            templates.add(template)
        }
        return templates;
    }

}
