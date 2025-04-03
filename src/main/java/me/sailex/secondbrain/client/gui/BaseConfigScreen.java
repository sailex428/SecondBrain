package me.sailex.secondbrain.client.gui;

import io.wispforest.owo.ui.container.FlowLayout;
import me.sailex.secondbrain.client.networking.ClientNetworkManager;
import me.sailex.secondbrain.config.BaseConfig;
import me.sailex.secondbrain.networking.packet.UpdateBaseConfigPacket;

public class BaseConfigScreen extends ConfigScreen<BaseConfig> {

    public BaseConfigScreen(
        ClientNetworkManager networkManager,
        BaseConfig baseConfig,
        boolean isEdit
    ) {
        super(networkManager, baseConfig, isEdit);
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        onPressSaveButton(rootComponent, button ->
            networkManager.sendPacket(new UpdateBaseConfigPacket(config))
        );
    }
}
