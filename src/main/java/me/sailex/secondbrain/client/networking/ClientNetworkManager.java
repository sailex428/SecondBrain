package me.sailex.secondbrain.client.networking;

import me.sailex.secondbrain.client.gui.SecondBrainScreen;
import me.sailex.secondbrain.networking.NetworkHandler;
import me.sailex.secondbrain.networking.packet.ConfigPacket;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;

public class ClientNetworkManager {

    private final MinecraftClient client;

    public ClientNetworkManager() {
        this.client = MinecraftClient.getInstance();
    }

    public void registerPacketReceiver() {
        NetworkHandler.CHANNEL.registerClientbound(ConfigPacket.class, (configPacket, access) -> {
            Screen npcConfigScreen = new SecondBrainScreen(configPacket.npcConfigs(), configPacket.baseConfig(), this);
            client.setScreen(npcConfigScreen);
        });
    }

    public <R extends Record> void sendPacket(R packet) {
        NetworkHandler.CHANNEL.clientHandle().send(packet);
    }
}
