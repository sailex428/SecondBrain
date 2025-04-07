package me.sailex.secondbrain.commands;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import me.sailex.secondbrain.common.NPCFactory;

import lombok.AllArgsConstructor;

import net.minecraft.server.command.ServerCommandSource;

@AllArgsConstructor
public class NPCRemoveCommand {

	private final NPCFactory npcFactory;

	public LiteralArgumentBuilder<ServerCommandSource> getCommand() {
		return literal("remove")
				.requires(source -> source.hasPermissionLevel(2))
				.then(argument("name", StringArgumentType.string()).executes(this::removeNPC));
	}

	private int removeNPC(CommandContext<ServerCommandSource> context) {
		String name = StringArgumentType.getString(context, "name");

		npcFactory.deleteNpc(name, context.getSource().getServer().getPlayerManager());
		return 1;
	}
}
