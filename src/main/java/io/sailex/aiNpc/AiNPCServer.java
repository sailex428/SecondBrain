package io.sailex.aiNpc;

import io.sailex.aiNpc.command.CommandManager;
import io.sailex.aiNpc.npc.NPCManager;
import io.sailex.aiNpc.service.OllamaService;
import io.sailex.aiNpc.util.config.ModConfig;
import net.fabricmc.api.ModInitializer;

public class AiNPCServer implements ModInitializer {

	@Override
	public void onInitialize() {
		ModConfig.init();

		OllamaService ollamaService = new OllamaService();

		NPCManager npcManager = new NPCManager();

		CommandManager commandManager = new CommandManager(npcManager);
		commandManager.register();
	}
}
