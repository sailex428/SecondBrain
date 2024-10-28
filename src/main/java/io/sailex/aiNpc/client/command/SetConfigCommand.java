package io.sailex.aiNpc.client.command;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import io.sailex.aiNpc.client.config.ModConfig;
import io.sailex.aiNpc.client.constant.ConfigConstants;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;

public class SetConfigCommand {

	public void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
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

	private int setConfig(CommandContext<FabricClientCommandSource> context) {
		String propertyKey = StringArgumentType.getString(context, "key");
		String propertyValue = StringArgumentType.getString(context, "value");

		if (!ConfigConstants.ALLOWED_KEYS.contains(propertyKey)) {
			context.getSource().sendFeedback(Text.of("Invalid property key!"));
			return 0;
		}

		if (ModConfig.setProperty(propertyKey, propertyValue)) {
			context.getSource().sendFeedback(Text.of("Saved property successfully!"));
			return 1;
		}

		context.getSource().sendFeedback(Text.of("Failed to save property!"));
		return 0;
	}
}
