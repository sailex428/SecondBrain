package me.sailex.secondbrain.version;

import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.CheckboxComponent;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.component.TextureComponent;
import io.wispforest.owo.ui.component.TextAreaComponent;
import io.wispforest.owo.ui.core.Sizing;

//? >=1.21.11 {
/*import io.wispforest.owo.ui.component.UIComponents;
*///?} else {

import io.wispforest.owo.ui.component.Components;
//?}

import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.function.Consumer;

public final class ComponentsVersion {

    private ComponentsVersion() {}

    public static TextAreaComponent textArea(Sizing horizontalSizing, Sizing verticalSizing) {
        //? >=1.21.11 {
        /*return UIComponents.textArea(horizontalSizing, verticalSizing);
        *///?} else {
        
        return Components.textArea(horizontalSizing, verticalSizing);
        //?}
    }

    public static LabelComponent label(Text text) {
        //? >=  1.21.11 {
        /*return UIComponents.label(text);
        *///?} else {
        return Components.label(text);
        //?}
    }

    public static CheckboxComponent checkbox(Text text) {
        //? >=  1.21.11 {
        /*return UIComponents.checkbox(text);
        *///?} else {
        return Components.checkbox(text);
        //?}
    }

    public static ButtonComponent button(Text text, Consumer<ButtonComponent> onPress) {
        //? >=  1.21.11 {
        /*return UIComponents.button(text, onPress);
        *///?} else {
        return Components.button(text, onPress);
        //?}
    }

    public static TextureComponent texture(Identifier texture, int u, int v, int regionWidth, int regionHeight, int textureWidth, int textureHeight) {
        //? >=  1.21.11 {
        /*return UIComponents.texture(texture, u, v, regionWidth, regionHeight, textureWidth, textureHeight);
        *///?} else {
        return Components.texture(texture, u, v, regionWidth, regionHeight, textureWidth, textureHeight);
        //?}
    }

}
