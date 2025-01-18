package me.sailex.ai.npc.commands;

import static net.minecraft.server.command.CommandManager.literal;

import me.sailex.ai.npc.config.ModConfig;
import lombok.AllArgsConstructor;
import me.sailex.ai.npc.npc.NPCFactory;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

/**
 * Command manager class that registers all commands.
 */
@AllArgsConstructor
public class CommandManager {

	private final ModConfig config;
	private final NPCFactory npcFactory;

	public void registerAll() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			new SetConfigCommand(config).register(dispatcher);
			dispatcher.register(literal("npc")
					.requires(source -> source.hasPermissionLevel(2))
					.then(new NPCCreateCommand(npcFactory).getCommand())
					.then(new NPCRemoveCommand(npcFactory).getCommand()));
		});
	}
}
