package me.sailex.secondbrain.commands;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import lombok.AllArgsConstructor;
import me.sailex.altoclef.multiversion.EntityVer;
import me.sailex.secondbrain.common.NPCService;
import me.sailex.secondbrain.config.NPCConfig;
import me.sailex.secondbrain.llm.LLMType;
import me.sailex.secondbrain.util.LogUtil;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

@AllArgsConstructor
public class NPCCreateCommand {

	private static final String LLM_TYPE = "llm-type";

	private final NPCService npcService;

	public LiteralArgumentBuilder<ServerCommandSource> getCommand() {
		return literal("add")
				.requires(source -> source.hasPermissionLevel(2))
				.then(argument("name", StringArgumentType.string())
						.then(argument(LLM_TYPE, StringArgumentType.string())
								.suggests((context, builder) -> {
									for (LLMType llmType : LLMType.getEntries()) {
										builder.suggest(llmType.toString());
									}
									return builder.buildFuture();
								}).executes(this::createNpcWithLLM)));
	}

	private int createNpcWithLLM(CommandContext<ServerCommandSource> context) {
		ServerPlayerEntity source = context.getSource().getPlayer();
		if (source == null) {
			context.getSource().sendFeedback(() -> LogUtil.formatError("Command must be executed as a Player!"), false);
			return 0;
		}

		String name = StringArgumentType.getString(context, "name");
		LLMType llmType = LLMType.valueOf(StringArgumentType.getString(context, LLM_TYPE));

		NPCConfig config = NPCConfig.builder(name).llmType(llmType).build();
		npcService.createNpc(config, EntityVer.getWorld(source).getServer(), source.getBlockPos(), source);
		return 1;
	}

}
