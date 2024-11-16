package io.sailex.aiNpc.client.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LogUtil {

	private static final Logger LOGGER = LogManager.getLogger(LogUtil.class);
	private static final MinecraftClient client = MinecraftClient.getInstance();

	public static void info(String message) {
		LOGGER.info(message);
		logInChat(message);
	}

	public static void error(String message) {
		LOGGER.error(message);
		logInChat(message);
	}

	private static void logInChat(String message) {
		ClientPlayNetworkHandler networkHandler = client.getNetworkHandler();
		if (networkHandler == null) {
			LOGGER.error("Network handler is null");
			return;
		}
		networkHandler.sendChatMessage(message);
	}
}
