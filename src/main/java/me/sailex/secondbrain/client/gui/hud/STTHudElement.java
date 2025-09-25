package me.sailex.secondbrain.client.gui.hud;

import lombok.Setter;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import static me.sailex.secondbrain.SecondBrain.MOD_ID;

//? >=1.21.6 {
/*import net.minecraft.client.gl.RenderPipelines;
*///?}

/**
 * Represents a HUD element that gets shown when speech-to-text gets started.
 * {@link me.sailex.secondbrain.client.keybind.STTKeybind}
 */
public class STTHudElement implements HudRenderCallback {

    private final MinecraftClient client = MinecraftClient.getInstance();

    @Setter
    private boolean isActive = false;

    @Override
    public void onHudRender(DrawContext drawContext, /*? <=1.20.1 {*/ float tickDelta /*?} else {*/  /*RenderTickCounter renderTickCounter  *//*?}*/) {
        if (isActive) {
            int screenCenter = drawContext.getScaledWindowWidth() / 2;
            drawContext.drawTexture(/*? >=1.21.6 {*/ /*RenderPipelines.GUI_TEXTURED, *//*?}*/ Identifier.of(MOD_ID, "stt-background.png"),
                    screenCenter - 40, 20,
                    80, 20,
                    0, 0,
                    80, 20, 80, 20);
            drawContext.drawTexture(/*? >=1.21.6 {*/ /*RenderPipelines.GUI_TEXTURED, *//*?}*/ Identifier.of(MOD_ID, "player2-icon.png"),
                    screenCenter - 35, 25,
                    9, 9,
                    0, 0,
                    55, 55, 55, 55);
            drawContext.drawText(client.textRenderer, Text.translatable("hud.element.stt"),
                    screenCenter - 23, 26,
                    0xFFFFFFFF, true);
        }
    }

}
