package me.sailex.secondbrain.client.gui;

import io.wispforest.owo.ui.base.BaseUIModelScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import me.sailex.secondbrain.client.networking.ClientNetworkManager;
import me.sailex.secondbrain.config.Configurable;
import net.minecraft.util.Identifier;

import java.util.function.Consumer;

import static me.sailex.secondbrain.SecondBrain.MOD_ID;

public abstract class ConfigScreen<T extends Configurable> extends BaseUIModelScreen<FlowLayout> {

    private static final Identifier ID = Identifier.of(MOD_ID, "config");
    protected final ClientNetworkManager networkManager;
    protected final T config;
    protected final boolean isEdit;

    protected ConfigScreen(
        ClientNetworkManager networkManager,
        T config,
        boolean isEdit
    ) {
        super(FlowLayout.class, DataSource.asset(ID));
        this.networkManager = networkManager;
        this.config = config;
        this.isEdit = isEdit;
    }

    protected abstract void build(FlowLayout rootComponent);

    /**
     * Performs callback function on press of save-button in rootComponent.
     *
     * @param rootComponent root layout component from screen
     * @param callback      consumer function
     */
    protected void onPressSaveButton(FlowLayout rootComponent, Consumer<ButtonComponent> callback) {
        rootComponent.childById(ButtonComponent.class, "save").onPress(callback);
    }
}
