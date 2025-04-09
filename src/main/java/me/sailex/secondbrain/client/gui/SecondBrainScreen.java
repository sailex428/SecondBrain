package me.sailex.secondbrain.client.gui;

import io.wispforest.owo.ui.base.BaseUIModelScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;

import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.Surface;
import me.sailex.secondbrain.client.networking.ClientNetworkManager;
import me.sailex.secondbrain.config.BaseConfig;
import me.sailex.secondbrain.config.NPCConfig;
import me.sailex.secondbrain.networking.packet.AddNpcPacket;
import me.sailex.secondbrain.networking.packet.DeleteNpcPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;

import static me.sailex.secondbrain.SecondBrain.MOD_ID;

public class SecondBrainScreen extends BaseUIModelScreen<FlowLayout> {

    private static final Identifier ID = Identifier.of(MOD_ID, "main");
    private final List<NPCConfig> npcConfig;
    private final BaseConfig baseConfig;
    private final ClientNetworkManager networkManager;

    public SecondBrainScreen(
        List<NPCConfig> npcConfig,
        BaseConfig baseConfig,
        ClientNetworkManager networkManager
    ) {
        super(FlowLayout.class, DataSource.asset(ID));
        this.npcConfig = npcConfig;
        this.baseConfig = baseConfig;
        this.networkManager = networkManager;
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        FlowLayout panelComponent = rootComponent.childById(FlowLayout.class, "npc").gap(1);
        npcConfig.forEach(config -> addNpcComponent(panelComponent, config));

        rootComponent.childById(ButtonComponent.class, "add_npc").onPress(button ->
            client.setScreen(new NPCConfigScreen(networkManager, new NPCConfig(), false))
        );

        rootComponent.childById(ButtonComponent.class, "edit_base").onPress(button ->
            client.setScreen(new BaseConfigScreen(networkManager, baseConfig, true))
        );
    }

    private void addNpcComponent(FlowLayout panelComponent, NPCConfig config) {
        panelComponent.child(Containers.verticalFlow(Sizing.content(), Sizing.content())
                .children(List.of(
                    Components.label(Text.of(config.getNpcName())),

                    Containers.horizontalFlow(Sizing.content(), Sizing.content()).children(List.of(

                            Components.button(isActiveText(config), button -> {
                                if (config.isActive()) {
                                    networkManager.sendPacket(new DeleteNpcPacket(config.getNpcName(), false));
                                } else {
                                    networkManager.sendPacket(new AddNpcPacket(config, false));
                                }
                                close();
                            }),

                            Components.button(Text.of("edit"), button ->
                                    client.setScreen(new NPCConfigScreen(networkManager, config, true))
                            ),

                            Components.button(Text.of("delete"), button -> {
                                networkManager.sendPacket(new DeleteNpcPacket(config.getNpcName(), true));
                                close();
                            })
                    )).gap(2)
                )).surface(Surface.DARK_PANEL).padding(Insets.of(10))
        );
    }

    private Text isActiveText(NPCConfig npcConfig) {
        return Text.of(npcConfig.isActive() ? "despawn" : "spawn");
    }

}
