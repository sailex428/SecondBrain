package me.sailex.secondbrain.client.gui;

import io.wispforest.owo.ui.container.FlowLayout;
import me.sailex.secondbrain.client.networking.ClientNetworkManager;
import me.sailex.secondbrain.config.NPCConfig;
import me.sailex.secondbrain.networking.packet.AddNpcPacket;
import me.sailex.secondbrain.networking.packet.UpdateNpcConfigPacket;

public class NPCConfigScreen extends ConfigScreen<NPCConfig> {

    public NPCConfigScreen(
        ClientNetworkManager networkManager,
        NPCConfig npcConfig,
        boolean isEdit
    ) {
        super(networkManager, npcConfig, isEdit);
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        onPressSaveButton(rootComponent, button -> {
            if (isEdit) {
                networkManager.sendPacket(new UpdateNpcConfigPacket(config));
            } else {
                networkManager.sendPacket(new AddNpcPacket(config, true));
            }
        });
    }
}
