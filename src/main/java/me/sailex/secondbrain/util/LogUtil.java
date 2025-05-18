package me.sailex.secondbrain.util;

import me.sailex.secondbrain.config.ConfigProvider;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LogUtil {

	private static MinecraftServer server;
	private static ConfigProvider configProvider;

	public static void initialize(MinecraftServer server, ConfigProvider configProvider) {
		LogUtil.server = server;
		LogUtil.configProvider = configProvider;
	}

	private LogUtil() {}

	private static final Logger LOGGER = LogManager.getLogger(LogUtil.class);
	private static final MutableText PREFIX = Text.literal("[SecondBrain] ").formatted(Formatting.DARK_PURPLE);

	private static MutableText formatDebug(String message) {
		return Text.literal(PREFIX.getString()).append(message).setStyle(Style.EMPTY.withFormatting(Formatting.DARK_GRAY));
	}

	public static MutableText formatInfo(String message) {
		return Text.literal("").append(PREFIX).append(message);
	}

	public static MutableText formatError(String message) {
		return Text.literal("").append(PREFIX).append(message).setStyle(Style.EMPTY.withFormatting(Formatting.RED));
	}

	public static String formatExceptionMessage(String message) {
		int messageBegin = message.indexOf(": ");
		if (messageBegin != -1) {
			return message.substring(messageBegin + 1);
		}
		return message;
	}

	public static void debugInChat(String message) {
		log(formatDebug(message));
	}

	public static void infoInChat(String message) {
		log(formatInfo(message));
	}

	public static void errorInChat(String message) {
		log(formatError(formatExceptionMessage(message)));
	}

	public static void info(String message) {
		if (configProvider.getBaseConfig().isVerbose()) LOGGER.info(formatInfo(message).getString());
	}

	public static void error(String message) {
		LOGGER.error(formatError(message).getString());
	}

	public static void error(String message, Throwable e) {
		LOGGER.error(formatError(message).getString(), e);
	}

	public static void error(Throwable e) {
		LOGGER.error(e.getMessage(), e);
	}

	private static void log(MutableText formattedMessage) {
		if (server != null) {
			server.getPlayerManager().getPlayerList().stream()
					.filter(player -> player.hasPermissionLevel(2))
					.forEach(player -> player.sendMessage(formattedMessage, false));
		} else {
			LOGGER.error("{}server is null - cant log to ingame chat!", PREFIX.getString());
		}
	}
}
