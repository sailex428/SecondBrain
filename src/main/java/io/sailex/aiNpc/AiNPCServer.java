package io.sailex.aiNpc;

import io.sailex.aiNpc.command.CommandManager;
import io.sailex.aiNpc.config.ConfigReader;
import io.sailex.aiNpc.constant.ConfigConstants;
import io.sailex.aiNpc.npc.NPCManager;
import net.fabricmc.api.ModInitializer;

public class AiNPCServer implements ModInitializer {

	@Override
	public void onInitialize() {
		ConfigReader configReader = new ConfigReader(ConfigConstants.PROPERTIES_FILE);

		NPCManager npcManager = new NPCManager(configReader);

		CommandManager commandManager = new CommandManager(npcManager);
		commandManager.register();
	}
}
