package io.sailex.aiNpc.command;

import io.sailex.aiNpc.npc.NPCManager;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public class CommandManager {

	private final CreateNPCCommand createNPCCommand;

	public CommandManager(NPCManager NPCManager) {
		createNPCCommand = new CreateNPCCommand(NPCManager);
	}

	public void register() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> createNPCCommand.register(dispatcher));
	}
}
