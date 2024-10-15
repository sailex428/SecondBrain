package io.sailex.aiNpc.util;

import io.sailex.aiNpc.constant.DefaultConstants;
import java.util.function.Supplier;
import net.minecraft.text.Text;

public class FeedbackLogger {

	public static Supplier<Text> logError(String message) {
		return () ->
				Text.literal(DefaultConstants.LOGGER_PREFIX + message).withColor(DefaultConstants.LOGGER_COLOR_ERROR);
	}

	public static Supplier<Text> logInfo(String message) {
		return () ->
				Text.literal(DefaultConstants.LOGGER_PREFIX + message).withColor(DefaultConstants.LOGGER_COLOR_INFO);
	}
}
