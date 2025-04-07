package me.sailex.secondbrain.model.context;

import net.minecraft.util.math.BlockPos;

public record StateData(BlockPos position, float health, int food, String biome) {}
