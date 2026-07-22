package me.sailex.secondbrain.client.gui.screen;

import io.wispforest.owo.ui.component.CheckboxComponent;
import io.wispforest.owo.ui.component.DropdownComponent;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.component.TextAreaComponent;
import io.wispforest.owo.ui.container.FlowLayout;

import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.Sizing;
import me.sailex.secondbrain.client.networking.ClientNetworkManager;
import me.sailex.secondbrain.config.BaseLLMConfig;
import me.sailex.secondbrain.config.NPCConfig;
import me.sailex.secondbrain.config.OllamaConfig;
import me.sailex.secondbrain.config.OpenAiConfig;
import me.sailex.secondbrain.config.Player2Config;
import me.sailex.secondbrain.llm.LLMType;
import me.sailex.secondbrain.networking.packet.CreateNpcPacket;
import me.sailex.secondbrain.networking.packet.UpdateNpcConfigPacket;

import me.sailex.secondbrain.version.ComponentsVersion;
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
            TextAreaComponent npcName = ComponentsVersion.textArea(Sizing.fill(35), Sizing.fill(7))
                    .text(config.getNpcName());
            npcName.onChanged().subscribe(config::setNpcName);
            panel.childById(FlowLayout.class, "npcName").child(npcName);
        }

        drawLLMTypeDropDown(panel);

        //draw without any dropdown click the fields of active llmType
        drawLLMModelInput(panel);
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

        if (config.getLlm() instanceof OllamaConfig llmConfig) {
            drawUrlInput(llmInfo, llmConfig);
        } else if (config.getLlm() instanceof Player2Config llmConfig) {
            CheckboxComponent isTTS = ComponentsVersion.checkbox(Text.of(NPCConfig.IS_TTS))
                    .checked(llmConfig.isTTS())
                    .onChanged(listener -> llmConfig.setTTS(!llmConfig.isTTS()));
            llmInfo.child(isTTS);
        } else if (config.getLlm() instanceof OpenAiConfig llmConfig) {
            drawUrlInput(llmInfo, llmConfig);
            TextAreaComponent llmInfoTextArea = ComponentsVersion.textArea(Sizing.fill(35), Sizing.fill(7));
            llmInfo.child(ComponentsVersion.label(Text.of(NPCConfig.OPENAI_API_KEY)).shadow(true).margins(Insets.top(7)));
            llmInfoTextArea.text(llmConfig.getApiKey())
                    .onChanged()
                    .subscribe(llmConfig::setApiKey);
            llmInfo.child(llmInfoTextArea);
        }

        //system prompt
        llmInfo.child(ComponentsVersion.label(Text.of(NPCConfig.LLM_CHARACTER)).shadow(true).margins(Insets.top(7)));
        TextAreaComponent llmCharacter = ComponentsVersion.textArea(Sizing.fill(35), Sizing.fill(20));
        llmCharacter.text(config.getLlmCharacter())
                .onChanged()
                .subscribe(config::setLlmCharacter);
        llmInfo.child(llmCharacter);
    }

    private void drawLLMTypeDropDown(FlowLayout panel) {
        panel.childById(LabelComponent.class, "llmType-label").text(Text.of(NPCConfig.LLM_TYPE));
        DropdownComponent llmTypeDropDown = panel.childById(DropdownComponent.class, "llmType");
        if (isEdit) {
            llmTypeDropDown.button(
                    Text.of(config.getLlm().getType().toString()), button -> {});
        } else {
            llmTypeDropDown.button(
                    Text.of(LLMType.OLLAMA.toString()),
                    button -> {
                        config.setLlm(new OllamaConfig());
                        drawLLMModelInput(panel);
                        drawLlmInfo(panel);
                    });
            llmTypeDropDown.button(
                    Text.of(LLMType.OPENAI.toString()),
                    button -> {
                        config.setLlm(new OpenAiConfig());
                        drawLLMModelInput(panel);
                        drawLlmInfo(panel);
                    });
        }
    }

    private void drawLLMModelInput(FlowLayout panel) {
        FlowLayout container = panel.childById(FlowLayout.class, "llmModel");
        container.clearChildren();

        if (config.getLlm() instanceof BaseLLMConfig llmConfig) {
            LabelComponent label = ComponentsVersion.label(Text.of(NPCConfig.LLM_MODEL)).shadow(true);
            TextAreaComponent llmModelInput = ComponentsVersion.textArea(Sizing.fill(21), Sizing.fill(7))
                    .text(llmConfig.getModel());
            llmModelInput.onChanged().subscribe(llmConfig::setModel);

            container.child(label);
            container.child(llmModelInput);
        }
    }

    private void drawUrlInput(FlowLayout llmInfo, BaseLLMConfig llmConfig) {
        TextAreaComponent llmInfoTextArea = ComponentsVersion.textArea(Sizing.fill(35), Sizing.fill(7));
        llmInfo.child(ComponentsVersion.label(Text.of(NPCConfig.URL)).shadow(true));
        llmInfoTextArea.text(llmConfig.getUrl())
                .onChanged()
                .subscribe(llmConfig::setUrl);
        llmInfo.child(llmInfoTextArea);
    }
}
