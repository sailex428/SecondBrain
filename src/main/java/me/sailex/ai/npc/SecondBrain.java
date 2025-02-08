package me.sailex.ai.npc;

import me.sailex.ai.npc.commands.CommandManager;
import me.sailex.ai.npc.config.ModConfig;
import lombok.Getter;
import me.sailex.ai.npc.database.SqliteClient;
import me.sailex.ai.npc.database.repositories.RepositoryFactory;
import me.sailex.ai.npc.database.resources.ResourcesProvider;
import me.sailex.ai.npc.npc.NPCFactory;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;

/**
 * Main class for the SecondBrain mod.
 */
@Getter
public class SecondBrain implements ModInitializer {

	public static final String MOD_ID = "second-brain";
	public static MinecraftServer server; //used for logUtil

	@Override
	public void onInitialize() {
		ModConfig config = new ModConfig();
		SqliteClient sqlite = new SqliteClient();

		RepositoryFactory repositoryFactory = new RepositoryFactory(sqlite);
		repositoryFactory.initRepositories();

		NPCFactory npcFactory = new NPCFactory(config, repositoryFactory);

		CommandManager commandManager = new CommandManager(config, npcFactory);
		commandManager.registerAll();

		ServerLifecycleEvents.SERVER_STARTED.register(server -> SecondBrain.server = server);
		ServerLifecycleEvents.SERVER_STOPPING.register(i -> onStop(npcFactory));
	}

	private void onStop(NPCFactory npcFactory) {
		ResourcesProvider resourcesProvider = npcFactory.getResourcesProvider();
		if (resourcesProvider != null) resourcesProvider.saveResources();
		npcFactory.shutdownNpc();
	}
}
