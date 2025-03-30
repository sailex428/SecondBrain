package me.sailex.secondbrain.client;

import me.sailex.secondbrain.client.networking.ClientNetworkManager;
import net.fabricmc.api.ClientModInitializer;

public class SecondBrainClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientNetworkManager networkManager = new ClientNetworkManager();
        networkManager.registerPacketReceiver();
    }
}
