package io.sailex.aiNpc.command;

import static net.minecraft.server.command.CommandManager.literal;

import io.sailex.aiNpc.npc.NPCManager;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public class CommandManager {

	private final CreateNPCCommand createNPCCommand;
	private final RemoveNPCCommand removeNPCCommand;

	public CommandManager(NPCManager NPCManager) {
		createNPCCommand = new CreateNPCCommand(NPCManager);
		removeNPCCommand = new RemoveNPCCommand(NPCManager);
	}

	public void register() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(
				literal("npc").then(createNPCCommand.getCommand()).then(removeNPCCommand.getCommand())));
	}
}
