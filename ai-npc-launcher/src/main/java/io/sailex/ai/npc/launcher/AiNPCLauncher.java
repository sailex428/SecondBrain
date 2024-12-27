package io.sailex.ai.npc.launcher;

import io.sailex.ai.npc.launcher.commands.CommandManager;
import io.sailex.ai.npc.launcher.config.AuthConfig;
import io.sailex.ai.npc.launcher.config.LauncherConfig;
import io.sailex.ai.npc.launcher.launcher.ClientLauncher;
import io.sailex.ai.npc.launcher.launcher.ClientProcessManager;
import io.sailex.ai.npc.launcher.launcher.NPCAuth;
import lombok.Getter;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;

public class AiNPCLauncher implements ModInitializer {

	@Getter
	private static MinecraftServer server;

	public static final String MOD_ID = "ai-npc-launcher";

	@Override
	public void onInitialize() {
		LauncherConfig launcherConfig = new LauncherConfig();
		AuthConfig authConfig = new AuthConfig();

		NPCAuth npcAuth = new NPCAuth(authConfig);
		ClientProcessManager npcClientProcessManager = new ClientProcessManager();
		ClientLauncher clientLauncher = new ClientLauncher(npcClientProcessManager, launcherConfig, npcAuth);
		npcAuth.setClientLauncher(clientLauncher);

		CommandManager commandManager = new CommandManager(clientLauncher, npcClientProcessManager, launcherConfig);
		commandManager.registerAll();

		ServerLifecycleEvents.SERVER_STARTED.register(minecraftServer -> {
			server = minecraftServer;
			clientLauncher.initLauncherAsync();
		});
	}
}
