package io.sailex.aiNpc.pathfinding;

import net.minecraft.util.math.BlockPos;

public class PathNode implements Comparable<PathNode> {

	protected BlockPos pos;
	protected PathNode parent;
	protected Movement movement;
	protected double costFromStart = Double.MAX_VALUE; // Cost from start
	protected double estimatedTotalCost = Double.MAX_VALUE; // Estimated total cost

	public PathNode(BlockPos pos, PathNode parent) {
		this.pos = pos;
		this.parent = parent;
	}

	@Override
	public int compareTo(PathNode other) {
		return Double.compare(this.estimatedTotalCost, other.estimatedTotalCost);
	}
}
