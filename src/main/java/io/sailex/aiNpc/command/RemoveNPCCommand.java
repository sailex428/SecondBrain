package io.sailex.aiNpc.command;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.sailex.aiNpc.npc.NPCManager;
import net.minecraft.server.command.ServerCommandSource;

public class RemoveNPCCommand {

	private final NPCManager npcManager;

	public RemoveNPCCommand(NPCManager npcManager) {
		this.npcManager = npcManager;
	}

	public LiteralArgumentBuilder<ServerCommandSource> getCommand() {
		return literal("remove")
				.then(argument("name", StringArgumentType.string()).executes(this::removeNPC));
	}

	private int removeNPC(CommandContext<ServerCommandSource> context) {
		String name = StringArgumentType.getString(context, "name");
		context.getSource()
				.sendFeedback(npcManager.removeNPC(name, context.getSource().getServer()), true);
		return 1;
	}
}
