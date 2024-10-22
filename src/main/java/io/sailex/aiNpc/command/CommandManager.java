package io.sailex.aiNpc.command;

import static net.minecraft.server.command.CommandManager.literal;

import io.sailex.aiNpc.npc.NPCManager;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public class CommandManager {

	private final NPCManager npcManager;

	public CommandManager(NPCManager NPCManager) {
		this.npcManager = NPCManager;
	}

	public void register() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			new SetConfigCommand().register(dispatcher);
			dispatcher.register(literal("npc")
					.requires(source -> source.hasPermissionLevel(2))
					.then(new NPCCreateCommand(npcManager).getCommand())
					.then(new NPCRemoveCommand(npcManager).getCommand())
					.then(new NPCDoCommand(npcManager.getNpcList()).getCommand()));
		});
	}
}
