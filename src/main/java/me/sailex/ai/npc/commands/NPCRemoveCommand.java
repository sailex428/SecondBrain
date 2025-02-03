package me.sailex.ai.npc.commands;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import me.sailex.ai.npc.npc.NPCFactory;
import me.sailex.ai.npc.util.LogUtil;
import lombok.AllArgsConstructor;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

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

		LogUtil.info("Try removing NPC with name: " + name);

		boolean isPlayerRemoved = removePlayer(name, context.getSource().getServer().getPlayerManager());

		//try first to remove npc else real players could be removed
		if (!npcFactory.removeNpc(name) || !isPlayerRemoved) {
			context.getSource().sendFeedback(() ->
					LogUtil.formatError("Could not find npc with name " + name), false);
			return 0;
		}
		return 1;
	}

	private boolean removePlayer(String name, PlayerManager playerManager) {
		ServerPlayerEntity player = playerManager.getPlayer(name);
		if (player != null) {
			playerManager.remove(player);
			return true;
		}
		return false;
	}
}
