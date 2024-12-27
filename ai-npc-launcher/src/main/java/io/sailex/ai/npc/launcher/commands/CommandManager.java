package io.sailex.ai.npc.launcher.commands;

import static net.minecraft.server.command.CommandManager.literal;

import io.sailex.ai.npc.launcher.config.LauncherConfig;
import io.sailex.ai.npc.launcher.launcher.ClientLauncher;
import io.sailex.ai.npc.launcher.launcher.ClientProcessManager;
import lombok.AllArgsConstructor;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

/**
 * Command manager class that registers all commands.
 */
@AllArgsConstructor
public class CommandManager {

	private final ClientLauncher clientLauncher;
	private final ClientProcessManager clientProcessManager;
	private final LauncherConfig launcherConfig;

	public void registerAll() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			new SetConfigCommand(launcherConfig).register(dispatcher);
			dispatcher.register(literal("npc")
					.requires(source -> source.hasPermissionLevel(2))
					.then(new NPCCreateCommand(clientLauncher, launcherConfig).getCommand())
					.then(new NPCRemoveCommand(clientProcessManager).getCommand()));
		});
	}
}
