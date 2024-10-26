package io.sailex.aiNpc.pathfinding;

import net.minecraft.util.math.BlockPos;

public record Movement(MovementType type, BlockPos destination, double cost) {}
