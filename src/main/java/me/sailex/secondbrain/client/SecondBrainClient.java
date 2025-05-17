package me.sailex.secondbrain.client;

import me.sailex.secondbrain.client.gui.hud.STTHudElement;
import me.sailex.secondbrain.client.keybind.STTKeybind;
import me.sailex.secondbrain.client.networking.ClientNetworkManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

public class SecondBrainClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientNetworkManager networkManager = new ClientNetworkManager();
        networkManager.registerPacketReceiver();

        STTHudElement sttHudElement = new STTHudElement();

        STTKeybind keybind = new STTKeybind(networkManager, sttHudElement);
        keybind.register();

        HudRenderCallback.EVENT.register(sttHudElement);
    }
}
