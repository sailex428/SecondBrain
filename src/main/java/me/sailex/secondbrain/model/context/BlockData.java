package me.sailex.secondbrain.model.context;

import net.minecraft.util.math.BlockPos;

public record BlockData(
    String type,
    BlockPos position,
    String mineLevel,
    String toolNeeded
) {
    @Override
    public String toString() {
        return "BlockData{" +
                "type='" + type + '\'' +
                ", position=" + position +
                '}';
    }
}
