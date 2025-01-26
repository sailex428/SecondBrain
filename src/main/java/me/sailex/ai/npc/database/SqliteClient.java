package me.sailex.ai.npc.database;

import me.sailex.ai.npc.SecondBrain;
import java.io.File;
import java.sql.*;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * SQLite client for managing the database.
 */
public class SqliteClient {

	private static final Logger LOGGER = LogManager.getLogger(SqliteClient.class);
	private Connection connection;

	/**
	 * Create the database and tables.
	 */
	public void initDatabase(String databaseName) {
		String databasePath = initDataBaseDir();
		try {
			String jdbcUrl = String.format("jdbc:sqlite:%s/%s.db", databasePath, databaseName);
			connection = DriverManager.getConnection(jdbcUrl);
			if (connection.isValid(3)) {
				LOGGER.info("Connected to database at: {}", databasePath);
			}
		} catch (SQLException e) {
			LOGGER.error("Error creating/connecting to database: {}", e.getMessage());
		}
	}

	private String initDataBaseDir() {
		File configDir = FabricLoader.getInstance().getConfigDir().toFile();
		File sqlDbDir = new File(configDir, SecondBrain.MOD_ID);
		if (sqlDbDir.mkdirs()) {
			LOGGER.info("Database directory created at: {}", sqlDbDir.getAbsolutePath());
		}
		return sqlDbDir.getAbsolutePath();
	}

	/**
	 * Select data from the database.
	 * @param sql the SQL query
	 */
	public ResultSet select(String sql) {
		try {
			Statement statement = connection.createStatement();
			statement.closeOnCompletion();
			return statement.executeQuery(sql);
		} catch (SQLException e) {
			LOGGER.error("Error selecting rule: {}", e.getMessage());
			return null;
		}
	}

	/**
	 * Insert data into the database.
	 * @param statement the prepared statement
	 */
	public void insert(PreparedStatement statement) {
		try {
			statement.executeUpdate();
		} catch (SQLException e) {
			LOGGER.error("Error inserting statement: {} : {}", statement, e.getMessage());
		}
	}

	public PreparedStatement buildPreparedStatement(String sql) {
		try {
			return connection.prepareStatement(sql);
		} catch (SQLException e) {
			LOGGER.error("Error building prepared statement: {}", e.getMessage());
			return null;
		}
	}

	/**
	 * Create a table in the database.
	 * @param sql the SQL query to create a table
	 */
	public void create(String sql) {
		try (Statement statement = connection.createStatement()) {
			statement.execute(sql);
		} catch (SQLException e) {
			LOGGER.error("Error executing query {} : {}", sql, e.getMessage());
		}
	}

	/**
	 * Close the database connection.
	 */
	public void closeConnection() {
		try {
			if (connection != null && !connection.isClosed()) {
				connection.close();
				LOGGER.info("Database connection closed.");
			}
		} catch (SQLException e) {
			LOGGER.error("Error closing database connection: {}", e.getMessage());
		}
	}
}
