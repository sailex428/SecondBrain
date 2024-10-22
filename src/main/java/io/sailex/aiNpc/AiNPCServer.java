package io.sailex.aiNpc;

import io.sailex.aiNpc.command.CommandManager;
import io.sailex.aiNpc.listener.EventListenerManager;
import io.sailex.aiNpc.npc.NPCManager;
import io.sailex.aiNpc.util.config.ModConfig;
import lombok.Getter;
import net.fabricmc.api.ModInitializer;

public class AiNPCServer implements ModInitializer {

	public static final String MOD_ID = "ai_npc";

	@Getter
	private static NPCManager npcManager;

	/**
	 * Initializes the server-side components of the AiNPC mod.
	 */
	@Override
	public void onInitialize() {
		ModConfig.init();

		npcManager = new NPCManager();

		EventListenerManager eventListenerManager = new EventListenerManager(npcManager.getNpcList());
		eventListenerManager.register();

		CommandManager commandManager = new CommandManager(npcManager);
		commandManager.register();
	}

}
