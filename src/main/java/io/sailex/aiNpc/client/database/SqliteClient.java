package io.sailex.aiNpc.client.database;

import io.sailex.aiNpc.client.AiNPCClient;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SqliteClient {

	private static final Logger LOGGER = LogManager.getLogger(SqliteClient.class);
	private static final String DATABASE_URL = "jdbc:sqlite:player_data.db";

	private Connection connection;

	public void createDatabase() {
		initDataBaseDir();
		try {
			connection = DriverManager.getConnection(DATABASE_URL);
			if (connection == null) {
				return;
			}
			if (connection.isValid(30)) {
				createTables();
				LOGGER.info("Database created or opened.");
			}
		} catch (SQLException e) {
			LOGGER.error("Error creating/connecting to database: {}", e.getMessage());
		}
	}

	private void createTables() {}

	private void initDataBaseDir() {
		File sqlDbDir = new File(FabricLoader.getInstance().getConfigDir().toFile() + AiNPCClient.MOD_ID + "sqlite_db");
		if (sqlDbDir.mkdirs()) {
			LOGGER.info("Database directory created.");
		}
	}
}
