package me.sailex.secondbrain;

import me.sailex.secondbrain.auth.PlayerAuthorizer;
import me.sailex.secondbrain.commands.CommandManager;
import me.sailex.secondbrain.common.NPCFactory;
import lombok.Getter;
import me.sailex.secondbrain.common.NPCService;
import me.sailex.secondbrain.common.Player2NpcSynchronizer;
import me.sailex.secondbrain.config.ConfigProvider;
import me.sailex.secondbrain.database.SqliteClient;
import me.sailex.secondbrain.database.repositories.RepositoryFactory;
import me.sailex.secondbrain.database.resources.ResourceProvider;
import me.sailex.secondbrain.listener.EventListenerRegisterer;
import me.sailex.secondbrain.networking.NetworkHandler;
import me.sailex.secondbrain.util.LogUtil;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.entity.player.PlayerEntity;

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

        ResourceProvider resourceProvider = new ResourceProvider(repositoryFactory.getConversationRepository());

        NPCFactory npcFactory = new NPCFactory(configProvider);
        NPCService npcService = new NPCService(npcFactory, configProvider, resourceProvider);

        PlayerAuthorizer authorizer = new PlayerAuthorizer();

        NetworkHandler networkManager = new NetworkHandler(configProvider, npcService, authorizer);
        networkManager.registerPacketReceiver();

        EventListenerRegisterer eventListenerRegisterer = new EventListenerRegisterer(npcService.getUuidToNpc());
        eventListenerRegisterer.register();

        Player2NpcSynchronizer synchronizer = new Player2NpcSynchronizer(npcService, configProvider);

        CommandManager commandManager = new CommandManager(npcService, configProvider, networkManager, synchronizer);
        commandManager.registerAll();

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            LogUtil.initialize(server, configProvider);
            repositoryFactory.initRepositories();
            resourceProvider.loadResources(configProvider.getUuidsOfNpcs());
            npcService.init();
        });

        syncOnPlayerLoad(synchronizer);
        onStop(npcService, configProvider, sqlite, synchronizer, resourceProvider);
    }

    private void syncOnPlayerLoad(Player2NpcSynchronizer synchronizer) {
        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            if (entity.isPlayer() && isFirstPlayerJoins) {
                synchronizer.syncCharacters(world.getServer(), (PlayerEntity) entity);
                isFirstPlayerJoins = false;
            }
        });
    }

	private void onStop(
        NPCService npcService,
        ConfigProvider configProvider,
        SqliteClient sqlite,
        Player2NpcSynchronizer synchronizer,
        ResourceProvider resourceProvider
	) {
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            synchronizer.shutdown();
            npcService.shutdownNPCs(server);
            resourceProvider.saveResources();
            sqlite.closeConnection();
            configProvider.saveAll();
            isFirstPlayerJoins = true;
        });
	}
}
