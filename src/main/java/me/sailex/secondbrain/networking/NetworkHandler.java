package me.sailex.secondbrain.networking;

import io.wispforest.owo.network.OwoNetChannel;
import me.sailex.secondbrain.auth.PlayerAuthorizer;
import me.sailex.secondbrain.callback.STTCallback;
import me.sailex.secondbrain.common.NPCFactory;
import me.sailex.secondbrain.config.BaseConfig;
import me.sailex.secondbrain.config.ConfigProvider;
import me.sailex.secondbrain.config.NPCConfig;
import me.sailex.secondbrain.networking.packet.*;
import me.sailex.secondbrain.util.LogUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.PlayerManager;
import net.minecraft.util.Identifier;

import java.util.UUID;

import static me.sailex.secondbrain.SecondBrain.MOD_ID;

/**
 * Serverside NetworkHandler that sends and receives packets to/from the client
 */
public class NetworkHandler {

    public static final Identifier CONFIG_CHANNEL_ID = Identifier.of(MOD_ID, "config-channel");
    public static final OwoNetChannel CHANNEL = OwoNetChannel.create(CONFIG_CHANNEL_ID);
    private final ConfigProvider configProvider;
    private final NPCFactory npcFactory;
    private final PlayerAuthorizer authorizer;

    public NetworkHandler(ConfigProvider configProvider, NPCFactory npcFactory, PlayerAuthorizer authorizer) {
        this.configProvider = configProvider;
        this.npcFactory = npcFactory;
        this.authorizer = authorizer;
    }

    /**
     * Registers endecs for serialization and packet receivers serverside.
     */
    public void registerPacketReceiver() {
        registerEndecs();

        registerUpdateBaseConfig();
        registerUpdateNpcConfig();
        registerAddNpc();
        registerDeleteNpc();
        registerStartStopSTT();

        CHANNEL.registerClientboundDeferred(ConfigPacket.class);
    }

    /**
     * Sends config packet to client to update base and npc configs.
     *
     * @param packet       config packet that will be sent
     * @param targetClient client the packet will be sent to
     */
    public void sendPacket(ConfigPacket packet, PlayerEntity targetClient) {
        CHANNEL.serverHandle(targetClient).send(packet);
    }

    private void registerUpdateBaseConfig() {
        CHANNEL.registerServerbound(UpdateBaseConfigPacket.class, (configPacket, serverAccess) -> {
            if (authorizer.isAuthorized(serverAccess)) {
                configProvider.setBaseConfig(configPacket.baseConfig());
                LogUtil.info("Updated base config to: " + configPacket);
            }
        });
    }

    private void registerUpdateNpcConfig() {
        CHANNEL.registerServerbound(UpdateNpcConfigPacket.class, (configPacket, serverAccess) -> {
            if (authorizer.isAuthorized(serverAccess)) {
                configProvider.updateNpcConfig(configPacket.npcConfig());
                LogUtil.info("Updated npc config to: " + configPacket);
            }
        });
    }

    private void registerAddNpc() {
        CHANNEL.registerServerbound(CreateNpcPacket.class, (createNpcPacket, serverAccess) -> {
            if (authorizer.isAuthorized(serverAccess)) {
                npcFactory.createNpc(createNpcPacket.npcConfig(), serverAccess.runtime(),
                        serverAccess.player().getBlockPos());
            }
        });
    }

    private void registerDeleteNpc() {
        CHANNEL.registerServerbound(DeleteNpcPacket.class, (configPacket, serverAccess) -> {
            if (authorizer.isAuthorized(serverAccess)) {
                PlayerManager playerManager = serverAccess.player().getServer().getPlayerManager();
                UUID uuid = UUID.fromString(configPacket.uuid());

                if (configPacket.isDelete()) {
                    npcFactory.deleteNpc(uuid, playerManager);
                } else {
                    npcFactory.removeNpc(uuid, playerManager);
                }
            }
        });
    }

    private void registerStartStopSTT() {
        CHANNEL.registerServerbound(STTPacket.class, (sttPacket, serverAccess) -> {
            if (authorizer.isAuthorized(serverAccess) && authorizer.isLocalConnection(serverAccess)) {
                STTCallback.EVENT.invoker().onSTTAction(sttPacket.type());
                LogUtil.info("STT action: " + sttPacket);
            }
        });
    }

    private void registerEndecs() {
        CHANNEL.addEndecs(builder -> {
            builder.register(ConfigPacket.ENDEC, ConfigPacket.class);
            builder.register(BaseConfig.ENDEC, BaseConfig.class);
            builder.register(NPCConfig.ENDEC, NPCConfig.class);
            builder.register(CreateNpcPacket.ENDEC, CreateNpcPacket.class);
            builder.register(DeleteNpcPacket.ENDEC, DeleteNpcPacket.class);
            builder.register(UpdateNpcConfigPacket.ENDEC, UpdateNpcConfigPacket.class);
            builder.register(UpdateBaseConfigPacket.ENDEC, UpdateBaseConfigPacket.class);
            builder.register(STTPacket.ENDEC, STTPacket.class);
        });
    }
}
