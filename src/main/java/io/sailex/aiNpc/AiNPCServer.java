package io.sailex.aiNpc;

import io.sailex.aiNpc.command.CommandManager;
import io.sailex.aiNpc.config.ConfigReader;
import io.sailex.aiNpc.constant.ConfigConstants;
import io.sailex.aiNpc.npc.NPCManager;
import net.fabricmc.api.ModInitializer;
import net.minecraft.server.MinecraftServer;

public class AiNPCServer implements ModInitializer {

	private MinecraftServer server;
	private CommandManager commandManager;
	private NPCManager npcManager;
	private ConfigReader configReader;

	@Override
	public void onInitialize() {
		configReader = new ConfigReader(ConfigConstants.PROPERTIES_FILE);

		npcManager = new NPCManager(server, configReader);

		commandManager = new CommandManager(npcManager);
		commandManager.register();
	}
}
