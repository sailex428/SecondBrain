package io.sailex.ai.npc.client.util;

import net.minecraft.client.MinecraftClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Utility class for logging messages to the console and in-game chat.
 */
public class LogUtil {

	private LogUtil() {}

	private static final Logger LOGGER = LogManager.getLogger(LogUtil.class);
	private static final MinecraftClient client = MinecraftClient.getInstance();

	/**
	 * Log an info message.
	 *
	 * @param message the info message
	 */
	public static void info(String message) {
		LOGGER.info(message);
		logInChat(message);
	}

	/**
	 * Log an error message.
	 *
	 * @param message the error message
	 */
	public static void error(String message) {
		LOGGER.error(message);
		logInChat(message);
	}

	private static void logInChat(String message) {
		if (client == null || client.getNetworkHandler() == null) {
			LOGGER.error("Network handler is null");
			return;
		}
		client.getNetworkHandler().sendChatMessage(message);
	}
}
