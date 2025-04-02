package me.sailex.secondbrain.client.gui;

import io.wispforest.owo.ui.base.BaseUIModelScreen;
import io.wispforest.owo.ui.container.FlowLayout;
import me.sailex.secondbrain.client.networking.ClientNetworkManager;
import me.sailex.secondbrain.config.Configurable;
import net.minecraft.util.Identifier;

import static me.sailex.secondbrain.SecondBrain.MOD_ID;

public abstract class ConfigScreen extends BaseUIModelScreen<FlowLayout> {

    private static final Identifier ID = Identifier.of(MOD_ID, "config");
    protected final ClientNetworkManager networkManager;
    protected final Configurable configurable;
    protected final boolean isEdit;

    protected ConfigScreen(
        ClientNetworkManager networkManager,
        Configurable configurable,
        boolean isEdit
    ) {
        super(FlowLayout.class, DataSource.asset(ID));
        this.networkManager = networkManager;
        this.configurable = configurable;
        this.isEdit = isEdit;
    }
}
