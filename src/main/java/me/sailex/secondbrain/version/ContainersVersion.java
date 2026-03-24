package me.sailex.secondbrain.version;

import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Sizing;

//? >= 1.21.11 {
/*import io.wispforest.owo.ui.container.UIContainers;
*///?} else {

import io.wispforest.owo.ui.container.Containers;
//?}

public final class ContainersVersion {

    private ContainersVersion() {}

    public static FlowLayout horizontalFlow(Sizing horizontalSizing, Sizing verticalSizing) {
        //? >= 1.21.11 {
        /*return UIContainers.horizontalFlow(horizontalSizing, verticalSizing);
        *///?} else {
        
        return Containers.horizontalFlow(horizontalSizing, verticalSizing);
         //?}
    }

    public static FlowLayout verticalFlow(Sizing horizontalSizing, Sizing verticalSizing) {
        //? >= 1.21.11 {
        /*return UIContainers.verticalFlow(horizontalSizing, verticalSizing);
        *///?} else {
        
        return Containers.verticalFlow(horizontalSizing, verticalSizing);
        //?}

    }
}
