package me.sailex.secondbrain;

import me.sailex.secondbrain.auth.PlayerAuthorizer;
import me.sailex.secondbrain.commands.CommandManager;
import me.sailex.secondbrain.common.NPCFactory;
import lombok.Getter;
import me.sailex.secondbrain.common.Player2NpcSynchronizer;
import me.sailex.secondbrain.config.ConfigProvider;
import me.sailex.secondbrain.database.SqliteClient;
import me.sailex.secondbrain.database.repositories.RepositoryFactory;
import me.sailex.secondbrain.database.resources.ResourcesProvider;
import me.sailex.secondbrain.listener.EventListenerRegisterer;
import me.sailex.secondbrain.networking.NetworkHandler;
import me.sailex.secondbrain.util.LogUtil;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;

/**
 * Main class for the SecondBrain mod.
 */
@Getter
public class SecondBrain implements ModInitializer {

	public static final String MOD_ID = "secondbrain";
	private boolean isFirstPlayerJoins = true;

	@Override
	public void onInitialize() {
		ConfigProvider configProvider = new ConfigProvider();

		SqliteClient sqlite = new SqliteClient();
		RepositoryFactory repositoryFactory = new RepositoryFactory(sqlite);
		repositoryFactory.initRepositories();

		NPCFactory npcFactory = NPCFactory.INSTANCE;

		PlayerAuthorizer authorizer = new PlayerAuthorizer();

		NetworkHandler networkManager = new NetworkHandler(configProvider, npcFactory, authorizer);
		networkManager.registerPacketReceiver();

		EventListenerRegisterer eventListenerRegisterer = new EventListenerRegisterer(npcFactory.getUuidToNpc());
		eventListenerRegisterer.register();

		Player2NpcSynchronizer synchronizer = new Player2NpcSynchronizer(npcFactory, configProvider);

		CommandManager commandManager = new CommandManager(npcFactory, configProvider, networkManager, synchronizer);
		commandManager.registerAll();

		ServerLifecycleEvents.SERVER_STARTED.register(server -> LogUtil.initialize(server, configProvider));

		ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
			if (entity.isPlayer() && isFirstPlayerJoins) {
				npcFactory.initialize(configProvider, repositoryFactory);
				synchronizer.initialize();
				synchronizer.setServer(world.getServer());
				synchronizer.syncCharacters();
				isFirstPlayerJoins = false;
			}
		});
		ServerLifecycleEvents.SERVER_STOPPING.register(server ->
				onStop(npcFactory, configProvider, sqlite, synchronizer, server));
	}

	private void onStop(
		NPCFactory npcFactory,
		ConfigProvider configProvider,
		SqliteClient sqlite,
		Player2NpcSynchronizer synchronizer,
		MinecraftServer server
	) {
		ResourcesProvider resourcesProvider = npcFactory.getResourcesProvider();
		if (resourcesProvider != null) resourcesProvider.saveResources();
		synchronizer.shutdown();
		npcFactory.shutdownNpcs(server);
		configProvider.saveAll();
		sqlite.closeConnection();
		isFirstPlayerJoins = true;
	}
}
