package me.sailex.secondbrain.client.gui;

import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.DropdownComponent;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.component.TextAreaComponent;
import io.wispforest.owo.ui.container.FlowLayout;

import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.Sizing;
import me.sailex.secondbrain.client.networking.ClientNetworkManager;
import me.sailex.secondbrain.config.NPCConfig;
import me.sailex.secondbrain.llm.LLMType;
import me.sailex.secondbrain.networking.packet.CreateNpcPacket;
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

        LabelComponent npcNameLabel = panel.childById(LabelComponent.class, "npcName-label");
        if (isEdit) {
            npcNameLabel.text(Text.of(NPCConfig.EDIT_NPC.formatted(config.getNpcName())));
        } else {
            npcNameLabel.text(Text.of(NPCConfig.NPC_NAME));
            TextAreaComponent npcName = Components.textArea(Sizing.fill(35), Sizing.fill(7))
                    .text(config.getNpcName());
            npcName.onChanged().subscribe(config::setNpcName);
            panel.childById(FlowLayout.class, "npcName").child(npcName);
        }

        panel.childById(LabelComponent.class, "llmType-label").text(Text.of(NPCConfig.LLM_TYPE));
        DropdownComponent llmTypeDropDown = panel.childById(DropdownComponent.class, "llmType");
//        LLMType.getEntries().forEach(type ->
            llmTypeDropDown.button(
                    Text.of(LLMType.OLLAMA.toString()),
                    button -> {
                        config.setLlmType(LLMType.OLLAMA);
                        drawLlmInfo(panel);
                    });
//        );
        //draw without any dropdown click the fields of active llmType
        drawLlmInfo(panel);

        onPressSaveButton(rootComponent, button -> {
            if (isEdit) {
                networkManager.sendPacket(new UpdateNpcConfigPacket(config));
                close();
            } else {
                networkManager.sendPacket(new CreateNpcPacket(config));
                close();
            }
        });
    }

    private void drawLlmInfo(FlowLayout panel) {
        FlowLayout llmInfo = panel.childById(FlowLayout.class, "llmInfo");
        llmInfo.clearChildren();

        //either show ollamaUrl or openai api key
        TextAreaComponent llmInfoTextArea = Components.textArea(Sizing.fill(35), Sizing.fill(7));
        switch (config.getLlmType()) {
            case OLLAMA -> {
                //ollama url
                llmInfo.child(Components.label(Text.of(NPCConfig.OLLAMA_URL)).shadow(true));
                llmInfoTextArea.text(config.getOllamaUrl())
                        .onChanged()
                        .subscribe(config::setOllamaUrl);
                llmInfo.child(llmInfoTextArea);

                //system prompt
                llmInfo.child(Components.label(Text.of(NPCConfig.LLM_CHARACTER)).shadow(true).margins(Insets.top(7)));
                TextAreaComponent llmCharacter = Components.textArea(Sizing.fill(35), Sizing.fill(25));
                llmCharacter.text(config.getLlmCharacter())
                        .onChanged()
                        .subscribe(config::setLlmCharacter);
                llmInfo.child(llmCharacter);

            }
            case OPENAI -> {
                llmInfo.child(Components.label(Text.of(NPCConfig.OPENAI_API_KEY)).shadow(true));
                llmInfoTextArea.text(config.getOpenaiApiKey())
                        .onChanged()
                        .subscribe(config::setOpenaiApiKey);
                llmInfo.child(llmInfoTextArea);
            }
        }
    }
}
