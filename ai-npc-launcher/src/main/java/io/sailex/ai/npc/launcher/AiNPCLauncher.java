package io.sailex.ai.npc.launcher;

import io.sailex.ai.npc.launcher.commands.CommandManager;
import io.sailex.ai.npc.launcher.config.ModConfig;
import io.sailex.ai.npc.launcher.launcher.ClientLauncher;
import io.sailex.ai.npc.launcher.launcher.ClientProcessManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;

public class AiNPCLauncher implements ModInitializer {

	public static MinecraftServer server;
	public static final String MOD_ID = "ai-npc-launcher";

	@Override
	public void onInitialize() {
		ModConfig.init();

		ClientProcessManager npcClientProcessManager = new ClientProcessManager();

		ClientLauncher clientLauncher = new ClientLauncher(npcClientProcessManager);

		CommandManager commandManager = new CommandManager(clientLauncher, npcClientProcessManager);
		commandManager.registerAll();

		ServerLifecycleEvents.SERVER_STARTED.register(minecraftServer -> {
			server = minecraftServer;
			clientLauncher.initLauncherAsync();
		});
	}
}
