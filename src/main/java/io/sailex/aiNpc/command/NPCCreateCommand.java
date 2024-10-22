package io.sailex.aiNpc.command;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.sailex.aiNpc.constant.DefaultConstants;
import io.sailex.aiNpc.model.command.NPCCommand;
import io.sailex.aiNpc.model.command.NPCState;
import io.sailex.aiNpc.npc.NPCManager;
import io.sailex.aiNpc.util.FeedbackLogger;
import java.util.Set;
import java.util.function.Supplier;
import lombok.AllArgsConstructor;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

@AllArgsConstructor
public class NPCCreateCommand {

	private static final String LLM_TYPE = "llm-type";
	private static final String LLM_MODEL = "llm-model";

	private static final Set<String> allowedLLMTypes = Set.of("ollama", "openai");

	private final NPCManager npcManager;

	public LiteralArgumentBuilder<ServerCommandSource> getCommand() {
		return literal("add")
				.then(argument("name", StringArgumentType.string())
						.executes(this::createNPCAtCurrentPosition)
						.then(argument(LLM_TYPE, StringArgumentType.string())
								.suggests((context, builder) -> {
									for (String llmType : allowedLLMTypes) {
										builder.suggest(llmType);
									}
									return builder.buildFuture();
								})
								.then(argument(LLM_MODEL, StringArgumentType.string())
										.executes(this::createNPCAtCurrentPosition)
										.then(argument("x", IntegerArgumentType.integer())
												.then(argument("y", IntegerArgumentType.integer())
														.then(argument("z", IntegerArgumentType.integer())
																.executes(this::createNPCAtSpecifiedPosition)))))));
	}

	private int createNPCAtCurrentPosition(CommandContext<ServerCommandSource> context) {
		String name = StringArgumentType.getString(context, "name");
		NPCState state = createNPCState(context, null);
		return createAndBuildNPC(context, name, state);
	}

	private int createNPCAtSpecifiedPosition(CommandContext<ServerCommandSource> context) {
		String name = StringArgumentType.getString(context, "name");
		Vec3d position = new Vec3d(
				IntegerArgumentType.getInteger(context, "x"),
				IntegerArgumentType.getInteger(context, "y"),
				IntegerArgumentType.getInteger(context, "z"));
		NPCState state = createNPCState(context, position);
		return createAndBuildNPC(context, name, state);
	}

	private NPCState createNPCState(CommandContext<ServerCommandSource> context, Vec3d specifiedPosition) {
		Vec3d position = specifiedPosition;
		if (position == null) {
			position = getPositionForNPC(context.getSource());
		}

		return new NPCState(
				position.x,
				position.y,
				position.z,
				null,
				DefaultConstants.NPC_BODY_YAW,
				DefaultConstants.NPC_HEAD_YAW,
				DefaultConstants.NPC_HEAD_PITCH,
				DefaultConstants.NPC_HEALTH,
				DefaultConstants.NPC_GAME_MODE);
	}

	private Vec3d getPositionForNPC(ServerCommandSource source) {
		Vec3d position = source.getPosition();
		if (position == null) {
			return source.getServer().getOverworld().getSpawnPos().toCenterPos();
		}
		return position;
	}

	private int createAndBuildNPC(CommandContext<ServerCommandSource> context, String name, NPCState state) {
		state.setDimension(context.getSource().getWorld().getRegistryKey());

		String llmType = null;
		String llmModel = null;

		if (hasLLMArguments(context)) {
			llmType = StringArgumentType.getString(context, LLM_TYPE);
			llmModel = StringArgumentType.getString(context, LLM_MODEL);

			if (!allowedLLMTypes.contains(llmType)) {
				context.getSource().sendFeedback(FeedbackLogger.logError("Invalid llm type!"), true);
				return 0;
			}
		}

		NPCCommand npcCommand = new NPCCommand(name, state);

		Supplier<Text> feedbackText =
				npcManager.buildNPC(npcCommand, context.getSource().getServer(), llmType, llmModel);
		context.getSource().sendFeedback(feedbackText, true);
		return 1;
	}

	private boolean hasLLMArguments(CommandContext<ServerCommandSource> context) {
		try {
			StringArgumentType.getString(context, LLM_TYPE);
			StringArgumentType.getString(context, LLM_MODEL);
			return true;
		} catch (IllegalArgumentException e) {
			return false;
		}
	}
}
