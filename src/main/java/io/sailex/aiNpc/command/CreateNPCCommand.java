package io.sailex.aiNpc.command;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.sailex.aiNpc.constant.DefaultConstants;
import io.sailex.aiNpc.model.NPC;
import io.sailex.aiNpc.model.NPCState;
import io.sailex.aiNpc.npc.NPCManager;
import java.util.function.Supplier;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

public class CreateNPCCommand {

	private final NPCManager npcManager;

	public CreateNPCCommand(NPCManager npcManager) {
		this.npcManager = npcManager;
	}

	public LiteralArgumentBuilder<ServerCommandSource> getCommand() {
		return literal("add")
				.then(argument("name", StringArgumentType.string())
						.executes(this::createNPCAtCurrentPosition)
						.then(argument("x", IntegerArgumentType.integer())
								.then(argument("y", IntegerArgumentType.integer())
										.then(argument("z", IntegerArgumentType.integer())
												.executes(this::createNPCAtSpecifiedPosition)))));
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
		NPC npc = new NPC(name, state);

		Supplier<Text> feedbackText =
				npcManager.buildNPC(npc, context.getSource().getServer());
		context.getSource().sendFeedback(feedbackText, true);
		return 1;
	}
}
