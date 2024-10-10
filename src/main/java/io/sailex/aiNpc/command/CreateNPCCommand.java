package io.sailex.aiNpc.command;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import io.sailex.aiNpc.constant.DefaultConstants;
import io.sailex.aiNpc.model.NPC;
import io.sailex.aiNpc.model.NPCState;
import io.sailex.aiNpc.npc.NPCManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.math.Vec3d;

public class CreateNPCCommand {

	private final NPCManager npcManager;

	public CreateNPCCommand(NPCManager npcManager) {
		this.npcManager = npcManager;
	}

	public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		dispatcher.register(literal("npc")
				.then(argument("name", StringArgumentType.string())
						.executes(this::createNPCAtCurrentPosition)
						.then(argument("x", IntegerArgumentType.integer())
								.then(argument("y", IntegerArgumentType.integer())
										.then(argument("z", IntegerArgumentType.integer())
												.executes(this::createNPCAtSpecifiedPosition))))));
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
				100,
				10,
				DefaultConstants.NPC_HEALTH,
				DefaultConstants.NPC_GAMEMODE);
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
		context.getSource()
				.sendFeedback(npcManager.buildNPC(npc, context.getSource().getServer()), false);
		return 1;
	}
}
