package me.sailex.secondbrain.client.gui;

import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.DropdownComponent;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.component.TextAreaComponent;
import io.wispforest.owo.ui.container.FlowLayout;

import io.wispforest.owo.ui.core.Sizing;
import me.sailex.secondbrain.client.networking.ClientNetworkManager;
import me.sailex.secondbrain.config.NPCConfig;
import me.sailex.secondbrain.llm.LLMType;
import me.sailex.secondbrain.networking.packet.AddNpcPacket;
import me.sailex.secondbrain.networking.packet.UpdateNpcConfigPacket;

import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import static me.sailex.secondbrain.SecondBrain.MOD_ID;

public class NPCConfigScreen extends ConfigScreen<NPCConfig> {

    private static final Identifier ID = Identifier.of(MOD_ID, "npcconfig");

    public NPCConfigScreen(
        ClientNetworkManager networkManager,
        NPCConfig npcConfig,
        boolean isEdit
    ) {
        super(networkManager, npcConfig, isEdit, ID);
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        FlowLayout panel = rootComponent.childById(FlowLayout.class, "panel");

        panel.childById(LabelComponent.class, "npcName-label").text(Text.of(NPCConfig.NPC_NAME));
        panel.childById(TextAreaComponent.class, "npcName")
                .text(config.getNpcName())
                .onChanged()
                .subscribe(config::setNpcName);

        panel.childById(LabelComponent.class, "llmDefaultPrompt-label").text(Text.of(NPCConfig.LLM_DEFAULT_PROMPT));
        panel.childById(TextAreaComponent.class, "llmDefaultPrompt")
                .text(config.getLlmDefaultPrompt())
                .onChanged()
                .subscribe(config::setLlmDefaultPrompt);

        panel.childById(LabelComponent.class, "llmType-label").text(Text.of(NPCConfig.LLM_TYPE));
        DropdownComponent llmTypeDropDown = panel.childById(DropdownComponent.class, "llmType");
        LLMType.getEntries().forEach(type ->
            llmTypeDropDown.button(
                    Text.of(type.toString()),
                    button -> {
                        config.setLlmType(type);
                        FlowLayout llmInfo = panel.childById(FlowLayout.class, "llmInfo");
                        llmInfo.clearChildren();
                        drawLlmInfo(llmInfo);
                    })
        );

        onPressSaveButton(rootComponent, button -> {
            if (isEdit) {
                networkManager.sendPacket(new UpdateNpcConfigPacket(config));
                close();
            } else {
                networkManager.sendPacket(new AddNpcPacket(config));
                close();
            }
        });
    }

    private void drawLlmInfo(FlowLayout panel) {
        //either show ollamaUrl or openai api key
        TextAreaComponent llmInfoTextArea = Components.textArea(Sizing.fill(35), Sizing.fill(7));
        switch (config.getLlmType()) {
            case OLLAMA -> {
                panel.child(Components.label(Text.of(NPCConfig.OLLAMA_URL)).shadow(true));
                llmInfoTextArea.text(config.getOllamaUrl())
                        .onChanged()
                        .subscribe(config::setOllamaUrl);
                panel.child(llmInfoTextArea);
            }
            case OPENAI -> {
                panel.child(Components.label(Text.of(NPCConfig.OPENAI_API_KEY)).shadow(true));
                llmInfoTextArea.text(config.getOpenaiApiKey())
                        .onChanged()
                        .subscribe(config::setOpenaiApiKey);
                panel.child(llmInfoTextArea);
            }
        }
    }
}
