package me.sailex.secondbrain.database.repositories

import me.sailex.secondbrain.config.NPCConfig
import me.sailex.secondbrain.database.SqliteClient
import me.sailex.secondbrain.llm.LLMType
import java.util.UUID

class NPCRepository(
    val sqliteClient: SqliteClient,
) {
    fun init() {
        createTable();
    }

    private fun createTable() {
        val sql = """
            CREATE TABLE IF NOT EXISTS npc (
                id uuid PRIMARY KEY,
                name varchar(16) NOT NULL,
                llmCharacter text NOT NULL,
                llmType varchar(7) NOT NULL,
                ollamaUrl varchar(2048) NOT NULL,
                llmModel varchar(32) NOT NULL,
                openaiApiKey varchar(255) NOT NULL,
                voiceId varchar(64) NOT NULL,
                skinUrl varchar(2048) NOT NULL,
                isTTS boolean NOT NULL
            );
        """;
        sqliteClient.query(sql)
    }

    fun insert(config: NPCConfig) {
        val statement =
            sqliteClient.buildPreparedStatement(
                "INSERT INTO npc (id, name, llmCharacter, llmType, ollamaUrl, llmModel, openaiApiKey, voiceId, skinUrl, isTTS) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
            )
        val npcConfigValues = arrayOf<String>(
            config.uuid.toString(), config.npcName, config.llmCharacter,
            config.llmType.toString(), config.ollamaUrl, config.llmModel,
            config.openaiApiKey, config.voiceId, config.skinUrl)

        for (i in npcConfigValues.indices) {
            statement.setString(i + 1, npcConfigValues[i])
        }
        statement.setBoolean(npcConfigValues.size + 1, config.isTTS)
        sqliteClient.insert(statement)
    }

    fun deleteByUuid(uuid: UUID) {
        val sql = "DELETE FROM npc WHERE uuid = '%s'".format(uuid.toString())
        sqliteClient.query(sql)
    }

    fun selectAllNpcs(): List<NPCConfig> {
        val sql = "SELECT * FROM npc"
        return executeAndProcess(sql)
    }

    private fun executeAndProcess(sql: String): List<NPCConfig>  {
        val result = sqliteClient.query(sql)
        val configs = arrayListOf<NPCConfig>()

        if (result == null) return configs

        while (result.next()) {
            val config =
                NPCConfig(
                    result.getString("name"),
                    result.getString("uuid"),
                    false,
                    result.getString("llmCharacter"),
                    LLMType.valueOf(result.getString("llmType")),
                    result.getString("llmModel"),
                    result.getString("ollamaUrl"),
                    result.getString("openaiApiKey"),
                    result.getBoolean("isTTS"),
                    result.getString("voiceId"),
                    result.getString("skinUrl")
                )
            configs.add(config)
        }
        result.close()
        return configs
    }

}