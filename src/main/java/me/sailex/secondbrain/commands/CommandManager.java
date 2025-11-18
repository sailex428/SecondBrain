package me.sailex.secondbrain.commands;

import static net.minecraft.server.command.CommandManager.literal;

import lombok.AllArgsConstructor;
import me.sailex.secondbrain.common.NPCFactory;
import me.sailex.secondbrain.common.NPCService;
import me.sailex.secondbrain.common.Player2NpcSynchronizer;
import me.sailex.secondbrain.config.ConfigProvider;
import me.sailex.secondbrain.networking.NetworkHandler;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

/**
 * Command manager class that registers all commands.
 */
@AllArgsConstructor
public class CommandManager {

	private final NPCService npcService;
	private final ConfigProvider configProvider;
	private final NetworkHandler networkHandler;
	private final Player2NpcSynchronizer synchronizer;

	public void registerAll() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
			dispatcher.register(literal("secondbrain")
					.requires(source -> source.hasPermissionLevel(2))
					.then(new NPCCreateCommand(npcService).getCommand())
					.then(new NPCRemoveCommand(npcService, configProvider).getCommand())
					.then(new Player2ActionCommand(synchronizer).getCommand())
					.executes(context -> new GuiCommand(configProvider, networkHandler).execute(context))
			)
		);
	}
}
