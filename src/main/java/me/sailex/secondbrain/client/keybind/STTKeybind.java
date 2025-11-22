package me.sailex.secondbrain.client.keybind;

import lombok.Getter;
import me.sailex.secondbrain.client.gui.hud.STTHudElement;
import me.sailex.secondbrain.client.networking.ClientNetworkManager;
import me.sailex.secondbrain.model.stt.STTType;
import me.sailex.secondbrain.networking.packet.STTPacket;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

@Getter
public class STTKeybind {

    /*? >=1.21.10 {*/
    /*private static final KeyBinding.Category CATEGORY = KeyBinding.Category.create(Identifier.of("secondbrain", "main"));
    *//*?}*/
    private final ClientNetworkManager networkManager;
    private final STTHudElement sttHudElement;
    private KeyBinding keyBinding;
    private boolean pressedLastTick = false;

    public STTKeybind(ClientNetworkManager networkManager, STTHudElement sttHudElement) {
        this.networkManager = networkManager;
        this.sttHudElement = sttHudElement;
    }

    public void register() {
        keyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "keybinding.tts",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_LEFT_ALT,
                /*? >=1.21.10 {*/ /*CATEGORY *//*?} else {*/ "SecondBrain" /*?}*/
        ));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            boolean isCurrentlyPressed = keyBinding.isPressed();
            if (isCurrentlyPressed && !pressedLastTick) {
                networkManager.sendPacket(new STTPacket(STTType.START));
                sttHudElement.setActive(true);
            } else if (!isCurrentlyPressed && pressedLastTick) {
                networkManager.sendPacket(new STTPacket(STTType.STOP));
                sttHudElement.setActive(false);
            }
            pressedLastTick = isCurrentlyPressed;
        });
    }

}
