package io.sailex.aiNpc.command;

import static net.minecraft.server.command.CommandManager.literal;

import io.sailex.aiNpc.npc.NPCManager;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public class CommandManager {

	private final CreateNPCCommand createNPCCommand;
	private final RemoveNPCCommand removeNPCCommand;
	private final SetConfigCommand setConfigCommand;

	public CommandManager(NPCManager NPCManager) {
		createNPCCommand = new CreateNPCCommand(NPCManager);
		removeNPCCommand = new RemoveNPCCommand(NPCManager);
		setConfigCommand = new SetConfigCommand();
	}

	public void register() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			setConfigCommand.register(dispatcher);
			dispatcher.register(literal("npc")
					.requires(source -> source.hasPermissionLevel(2))
					.then(createNPCCommand.getCommand())
					.then(removeNPCCommand.getCommand()));
		});
	}
}
