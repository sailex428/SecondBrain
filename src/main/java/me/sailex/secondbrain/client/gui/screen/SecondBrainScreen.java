package me.sailex.secondbrain.client.gui.screen;

import io.wispforest.owo.ui.base.BaseUIModelScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;

import io.wispforest.owo.ui.container.GridLayout;
import io.wispforest.owo.ui.core.HorizontalAlignment;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.Surface;
import io.wispforest.owo.ui.core.VerticalAlignment;
import me.sailex.secondbrain.client.networking.ClientNetworkManager;
import me.sailex.secondbrain.config.BaseConfig;
import me.sailex.secondbrain.config.NPCConfig;
import me.sailex.secondbrain.llm.LLMType;
import me.sailex.secondbrain.networking.packet.CreateNpcPacket;
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
    private int currentIndex = 0;

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
        drawCarouselItems(rootComponent);

        rootComponent.childById(ButtonComponent.class, "add_npc").onPress(button ->
                client.setScreen(new NPCConfigScreen(networkManager, new NPCConfig(), false))
        );

        rootComponent.childById(ButtonComponent.class, "edit_base").onPress(button ->
                client.setScreen(new BaseConfigScreen(networkManager, baseConfig, true))
        );
    }

    private void drawCarouselItems(FlowLayout rootComponent) {
        FlowLayout panelComponent = rootComponent.childById(FlowLayout.class, "npc");

        NPCConfig left = getConfigByPosition(CarouselPosition.LEFT);
        NPCConfig center = getConfigByPosition(CarouselPosition.CENTER);
        NPCConfig right = getConfigByPosition(CarouselPosition.RIGHT);

        if (center == null) {
            return;
        }

        drawCarouselItemOrEmpty(panelComponent, left);
        addNpcComponent(panelComponent, center, false);
        drawCarouselItemOrEmpty(panelComponent, right);
    }

    private void drawCarouselItemOrEmpty(FlowLayout panelComponent, NPCConfig config) {
        if (config != null) {
            addNpcComponent(panelComponent, config, true);
        } else {
            panelComponent.child(Containers.verticalFlow(Sizing.fixed(135), Sizing.content()));
        }
    }

    private NPCConfig getConfigByPosition(CarouselPosition position) {
        int index = currentIndex + position.getPosition();
        if (index < 0 || index >= npcConfig.size()) {
            return null;
        }
        return npcConfig.get(index);
    }

    private void addNpcComponent(FlowLayout panelComponent, NPCConfig config, boolean isFaded) {
        FlowLayout npcContainer = Containers.verticalFlow(Sizing.fixed(135), Sizing.content());
        npcContainer.margins(Insets.bottom(1));
        npcContainer.surface(Surface.DARK_PANEL).padding(Insets.of(10));

        FlowLayout npcLabelContainer = Containers.horizontalFlow(Sizing.content(), Sizing.content());
        npcLabelContainer.gap(2);
        npcLabelContainer.child(Components.label(Text.of(config.getNpcName())));
        addTypeSpecificLabelTexture(npcLabelContainer, config);
        npcContainer.child(npcLabelContainer);

        GridLayout npcButtonContainer = Containers.grid(Sizing.fill(100), Sizing.content(), 1, 3);

        npcButtonContainer.horizontalAlignment(HorizontalAlignment.CENTER);
        npcButtonContainer.verticalAlignment(VerticalAlignment.CENTER);

        addNpcSpawnButton(npcButtonContainer, config);
        addNpcEditButton(npcButtonContainer, config);
        addNpcDeleteButton(npcButtonContainer, config);

        npcContainer.child(npcButtonContainer);
        panelComponent.child(npcContainer);
    }

    private void addTypeSpecificLabelTexture(FlowLayout npcLabelContainer, NPCConfig config) {
        String iconPath;
        switch (config.getLlmType()) {
            case OLLAMA -> iconPath = "ollama-icon.png";
            case PLAYER2 -> iconPath = "player2-icon.png";
            default -> iconPath = "icon.png";
        }
        npcLabelContainer.child(Components.texture(Identifier.of(MOD_ID, iconPath), 0, 0,
                55, 55, 55, 55).sizing(Sizing.fixed(8), Sizing.fixed(8)));
    }

    private void addNpcSpawnButton(GridLayout npcButtonContainer, NPCConfig config) {
        npcButtonContainer.child(Components.button(isActiveText(config), button -> {
            if (config.isActive()) {
                networkManager.sendPacket(new DeleteNpcPacket(config.getUuid().toString(), false));
            } else {
                networkManager.sendPacket(new CreateNpcPacket(config));
            }
            close();
        }), 0, 0);
    }

    private void addNpcEditButton(GridLayout npcButtonContainer, NPCConfig config) {
        if (!config.isActive() || config.getLlmType() == LLMType.PLAYER2) {
            npcButtonContainer.child(Components.button(Text.of(" Edit "), button ->
                    client.setScreen(new NPCConfigScreen(networkManager, config, true))
            ), 0, 1);
        }
    }

    private void addNpcDeleteButton(GridLayout npcButtonContainer, NPCConfig config) {
        npcButtonContainer.child(Components.button(Text.of("Delete"), button -> {
            networkManager.sendPacket(new DeleteNpcPacket(config.getUuid().toString(), true));
            close();
        }), 0, config.isActive() ? 1 : 2);
    }

    private Text isActiveText(NPCConfig npcConfig) {
        return Text.of(npcConfig.isActive() ? "Despawn" : "Spawn");
    }

}
