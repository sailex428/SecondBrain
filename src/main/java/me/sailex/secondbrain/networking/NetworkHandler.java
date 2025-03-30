package me.sailex.secondbrain.networking;

import io.wispforest.owo.network.OwoNetChannel;
import io.wispforest.owo.network.ServerAccess;
import me.sailex.secondbrain.common.NPCFactory;
import me.sailex.secondbrain.config.BaseConfig;
import me.sailex.secondbrain.config.ConfigProvider;
import me.sailex.secondbrain.config.NPCConfig;
import me.sailex.secondbrain.networking.packet.AddNpcPacket;
import me.sailex.secondbrain.networking.packet.ConfigPacket;
import me.sailex.secondbrain.networking.packet.DeleteNpcPacket;
import me.sailex.secondbrain.util.LogUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

import static me.sailex.secondbrain.SecondBrain.MOD_ID;

public class NetworkHandler {

    public static final Identifier CONFIG_CHANNEL_ID = Identifier.of(MOD_ID, "config-channel");
    public static final OwoNetChannel CHANNEL = OwoNetChannel.create(CONFIG_CHANNEL_ID);
    private final ConfigProvider configProvider;
    private final NPCFactory npcFactory;

    public NetworkHandler(ConfigProvider configProvider, NPCFactory npcFactory) {
        this.configProvider = configProvider;
        this.npcFactory = npcFactory;
    }

    public void registerPacketReceiver() {
        CHANNEL.addEndecs(builder -> {
            builder.register(ConfigPacket.ENDEC, ConfigPacket.class);
            builder.register(BaseConfig.ENDEC, BaseConfig.class);
            builder.register(NPCConfig.ENDEC, NPCConfig.class);
            builder.register(AddNpcPacket.ENDEC, AddNpcPacket.class);
            builder.register(DeleteNpcPacket.ENDEC, DeleteNpcPacket.class);
        });

        CHANNEL.registerServerbound(ConfigPacket.class, (configPacket, clientAccess) -> {
            if (isPlayerAuthorized(clientAccess)) {
                configProvider.setBaseConfig(configPacket.baseConfig());
                //configProvider.setNpcConfigs(configPacket.npcConfigs());
                LogUtil.info("Updated config to: " + configPacket, true);
            }
        });
        CHANNEL.registerServerbound(AddNpcPacket.class, (addNpcPacket, clientAccess) -> {
            if (isPlayerAuthorized(clientAccess)) {
                configProvider.addNpcConfig(addNpcPacket.npcConfig());
                npcFactory.createNpc(addNpcPacket.npcConfig(), clientAccess.player());
                LogUtil.info("Added npc: " + addNpcPacket, true);
            }
        });
        CHANNEL.registerServerbound(DeleteNpcPacket.class, (configPacket, clientAccess) -> {
            if (isPlayerAuthorized(clientAccess)) {
                configProvider.deleteNpcConfig(configPacket.npcName());
                npcFactory.deleteNpc(configPacket.npcName());
                LogUtil.info("Deleted npc with uuid: " + configPacket.npcName(), true);
            }
        });

        CHANNEL.registerClientboundDeferred(ConfigPacket.class);
    }

    public void sendPacket(ConfigPacket packet, PlayerEntity targetClient) {
        CHANNEL.serverHandle(targetClient).send(packet);
    }

    private boolean isPlayerAuthorized(ServerAccess clientAccess) {
        return clientAccess.player().isCreativeLevelTwoOp();
    }
}
