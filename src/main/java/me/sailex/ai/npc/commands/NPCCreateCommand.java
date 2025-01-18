package me.sailex.ai.npc.commands;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import baritone.Baritone;
import baritone.BaritoneProvider;
import baritone.api.BaritoneAPI;
import baritone.api.IBaritone;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import lombok.AllArgsConstructor;
import me.sailex.ai.npc.exception.InvalidLLMTypeException;
import me.sailex.ai.npc.llm.LLMType;
import me.sailex.ai.npc.npc.NPCFactory;
import me.sailex.ai.npc.util.LogUtil;

import net.minecraft.server.PlayerManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

@AllArgsConstructor
public class NPCCreateCommand {

	private static final String LLM_TYPE = "llm-type";
	private static final String LLM_MODEL = "llm-model";
	private static final String IS_ONLINE_COMMAND = "isOnline";

	private final NPCFactory npcFactory;

	public LiteralArgumentBuilder<ServerCommandSource> getCommand() {
		return literal("add")
				.requires(source -> source.hasPermissionLevel(2))
				.then(argument("name", StringArgumentType.string())
						.then(argument(IS_ONLINE_COMMAND, BoolArgumentType.bool())
								.then(argument(LLM_TYPE, StringArgumentType.string())
										.suggests((context, builder) -> {
											for (LLMType llmType : LLMType.getEntries()) {
												builder.suggest(llmType.name());
											}
											return builder.buildFuture();
										})
										.then(argument(LLM_MODEL, StringArgumentType.string())
												.executes(this::createNpcWithLLM)))));
	}

	private int createNpcWithLLM(CommandContext<ServerCommandSource> context) {
		String name = StringArgumentType.getString(context, "name");
		boolean isOnline = BoolArgumentType.getBool(context, IS_ONLINE_COMMAND);
		String llmType = StringArgumentType.getString(context, LLM_TYPE);
		String llmModel = StringArgumentType.getString(context, LLM_MODEL);

		LogUtil.info(("Creating NPC with name: " + name + ", LLM Type: " + llmType + ", LLM Model: " + llmModel));

		try {
			ServerPlayerEntity npc = spawnNpc(context.getSource(), name);
			npcFactory.createNpc(context.getSource().getServer(), npc, llmType, llmModel);
			return 1;
		} catch (InvalidLLMTypeException e) {
			context.getSource().sendFeedback(() -> Text.literal(e.getMessage()), false);
			return 0;
		}
	}

	private ServerPlayerEntity spawnNpc(ServerCommandSource source, String name) {
		//
		// BaritoneProvider.INSTANCE.getBaritone(source.getPlayer()).getCommandHelper().executeSpawn(name);
		PlayerManager playerManager = source.getServer().getPlayerManager();
		return playerManager.getPlayer(name);
	}
}
