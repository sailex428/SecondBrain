package me.sailex.secondbrain.networking;

import io.wispforest.owo.network.OwoNetChannel;
import io.wispforest.owo.network.ServerAccess;
import me.sailex.secondbrain.common.NPCFactory;
import me.sailex.secondbrain.config.BaseConfig;
import me.sailex.secondbrain.config.ConfigProvider;
import me.sailex.secondbrain.config.NPCConfig;
import me.sailex.secondbrain.networking.packet.*;
import me.sailex.secondbrain.util.LogUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

import static me.sailex.secondbrain.SecondBrain.MOD_ID;

/**
 * Serverside NetworkHandler that sends and receives packets to/from the client
 */
public class NetworkHandler {

    public static final Identifier CONFIG_CHANNEL_ID = Identifier.of(MOD_ID, "config-channel");
    public static final OwoNetChannel CHANNEL = OwoNetChannel.create(CONFIG_CHANNEL_ID);
    private final ConfigProvider configProvider;
    private final NPCFactory npcFactory;

    public NetworkHandler(ConfigProvider configProvider, NPCFactory npcFactory) {
        this.configProvider = configProvider;
        this.npcFactory = npcFactory;
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
        CHANNEL.registerServerbound(UpdateBaseConfigPacket.class, (configPacket, clientAccess) -> {
            if (isPlayerAuthorized(clientAccess)) {
                configProvider.setBaseConfig(configPacket.baseConfig());
                LogUtil.info("Updated base config to: " + configPacket, true);
            }
        });
    }

    private void registerUpdateNpcConfig() {
        CHANNEL.registerServerbound(UpdateNpcConfigPacket.class, (configPacket, clientAccess) -> {
            if (isPlayerAuthorized(clientAccess)) {
                configProvider.updateNpcConfig(configPacket.npcConfig());
                LogUtil.info("Updated npc config to: " + configPacket, true);
            }
        });
    }

    private void registerAddNpc() {
        CHANNEL.registerServerbound(AddNpcPacket.class, (addNpcPacket, clientAccess) -> {
            if (isPlayerAuthorized(clientAccess)) {
                npcFactory.createNpc(addNpcPacket.npcConfig(), clientAccess.player());
                if (addNpcPacket.isNewConfig()) {
                    configProvider.addNpcConfig(addNpcPacket.npcConfig());
                }
                LogUtil.info("Added npc: " + addNpcPacket, true);
            }
        });
    }

    private void registerDeleteNpc() {
        CHANNEL.registerServerbound(DeleteNpcPacket.class, (configPacket, clientAccess) -> {
            if (isPlayerAuthorized(clientAccess)) {
                npcFactory.deleteNpc(configPacket.npcName());
                if (configPacket.isDeleteConfig()) {
                    configProvider.deleteNpcConfig(configPacket.npcName());
                }
                LogUtil.info("Deleted npc with uuid: " + configPacket, true);
            }
        });
    }

    private void registerEndecs() {
        CHANNEL.addEndecs(builder -> {
            builder.register(ConfigPacket.ENDEC, ConfigPacket.class);
            builder.register(BaseConfig.ENDEC, BaseConfig.class);
            builder.register(NPCConfig.ENDEC, NPCConfig.class);
            builder.register(AddNpcPacket.ENDEC, AddNpcPacket.class);
            builder.register(DeleteNpcPacket.ENDEC, DeleteNpcPacket.class);
            builder.register(UpdateNpcConfigPacket.ENDEC, UpdateNpcConfigPacket.class);
            builder.register(UpdateBaseConfigPacket.ENDEC, UpdateBaseConfigPacket.class);
        });
    }

    private boolean isPlayerAuthorized(ServerAccess clientAccess) {
        return clientAccess.player().isCreativeLevelTwoOp();
    }
}
