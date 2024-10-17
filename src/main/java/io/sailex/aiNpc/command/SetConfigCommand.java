package io.sailex.aiNpc.command;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import io.sailex.aiNpc.constant.ConfigConstants;
import io.sailex.aiNpc.util.FeedbackLogger;
import io.sailex.aiNpc.util.config.ModConfig;
import net.minecraft.server.command.ServerCommandSource;

public class SetConfigCommand {

	public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		dispatcher.register(literal("setconfig")
				.requires(source -> source.hasPermissionLevel(2))
				.then(argument("key", StringArgumentType.word())
						.suggests((context, builder) -> {
							for (String key : ConfigConstants.ALLOWED_KEYS) {
								builder.suggest(key);
							}
							return builder.buildFuture();
						})
						.then(argument("value", StringArgumentType.string()).executes(this::setConfig))));
	}

	private int setConfig(CommandContext<ServerCommandSource> context) {
		String propertyKey = StringArgumentType.getString(context, "key");
		String propertyValue = StringArgumentType.getString(context, "value");

		if (!ConfigConstants.ALLOWED_KEYS.contains(propertyKey)) {
			context.getSource().sendFeedback(FeedbackLogger.logError("Invalid property key!"), false);
			return 0;
		}

		if (ModConfig.setProperty(propertyKey, propertyValue)) {
			context.getSource().sendFeedback(FeedbackLogger.logInfo("Saved property successfully!"), false);
			return 1;
		}

		context.getSource().sendFeedback(FeedbackLogger.logError("Failed to save property!"), false);
		return 0;
	}
}
