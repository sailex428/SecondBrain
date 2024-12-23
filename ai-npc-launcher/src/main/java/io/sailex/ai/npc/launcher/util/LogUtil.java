package io.sailex.ai.npc.launcher.util;

import io.sailex.ai.npc.launcher.AiNPCLauncher;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LogUtil {

	private LogUtil() {}

	private static final Logger LOGGER = LogManager.getLogger(LogUtil.class);
	private static final MutableText PREFIX = buildPrefix();

	public static void info(String message) {
		info(message, false);
	}

	public static void info(String message, boolean onlyInConsole) {
		MutableText formattedMessage = formatInfo(message);
		if (onlyInConsole) {
			LOGGER.info(formattedMessage.getString());
		} else {
			log(formattedMessage);
		}
	}

	public static MutableText formatInfo(String message) {
		return Text.literal("").append(PREFIX).append(message);
	}

	public static void error(String message) {
		error(message, false);
	}

	public static void error(String message, boolean onlyInConsole) {
		MutableText formattedMessage = formatError(message);
		if (onlyInConsole) {
			LOGGER.error(formattedMessage.getString());
		} else {
			log(formattedMessage);
		}
	}

	public static MutableText formatError(String message) {
		return Text.literal("").append(PREFIX).append(message).setStyle(Style.EMPTY.withFormatting(Formatting.RED));
	}

	private static MutableText buildPrefix() {
		return Text.literal("[AI-NPC] ").formatted(Formatting.DARK_PURPLE);
	}

	private static void log(MutableText formattedMessage) {
		AiNPCLauncher.server.getPlayerManager().getPlayerList().stream()
				.filter(PlayerEntity::isCreativeLevelTwoOp)
				.forEach(player -> player.sendMessage(formattedMessage, false));
	}
}
