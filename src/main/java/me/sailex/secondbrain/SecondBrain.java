package me.sailex.secondbrain;

import me.sailex.secondbrain.commands.CommandManager;
import me.sailex.secondbrain.common.NPCFactory;
import lombok.Getter;
import me.sailex.secondbrain.config.ConfigProvider;
import me.sailex.secondbrain.database.SqliteClient;
import me.sailex.secondbrain.database.repositories.RepositoryFactory;
import me.sailex.secondbrain.database.resources.ResourcesProvider;
import me.sailex.secondbrain.listener.EventListenerRegisterer;
import me.sailex.secondbrain.networking.NetworkHandler;
import me.sailex.secondbrain.util.LogUtil;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;

/**
 * Main class for the SecondBrain mod.
 */
@Getter
public class SecondBrain implements ModInitializer {

	public static final String MOD_ID = "secondbrain";

	@Override
	public void onInitialize() {
		ConfigProvider configProvider = new ConfigProvider();

		SqliteClient sqlite = new SqliteClient();
		RepositoryFactory repositoryFactory = new RepositoryFactory(sqlite);
		repositoryFactory.initRepositories();

		NPCFactory npcFactory = NPCFactory.INSTANCE;
		npcFactory.initialize(configProvider, repositoryFactory);

		NetworkHandler networkManager = new NetworkHandler(configProvider, npcFactory);
		networkManager.registerPacketReceiver();

		EventListenerRegisterer eventListenerRegisterer = new EventListenerRegisterer(npcFactory.getNameToNpc());
		eventListenerRegisterer.register();

		CommandManager commandManager = new CommandManager(npcFactory, configProvider, networkManager);
		commandManager.registerAll();

		ServerLifecycleEvents.SERVER_STARTED.register(LogUtil::initialize);
		ServerLifecycleEvents.SERVER_STOPPING.register(server -> onStop(npcFactory, configProvider, sqlite, server));
	}

	private void onStop(NPCFactory npcFactory, ConfigProvider configProvider, SqliteClient sqlite, MinecraftServer server) {
		ResourcesProvider resourcesProvider = npcFactory.getResourcesProvider();
		if (resourcesProvider != null) resourcesProvider.saveResources();
		npcFactory.shutdownNpcs(server);
		configProvider.saveAll();
		sqlite.closeConnection();
	}
}
