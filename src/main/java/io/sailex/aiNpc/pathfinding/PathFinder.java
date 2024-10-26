package io.sailex.aiNpc.pathfinding;

import io.sailex.aiNpc.npc.NPCEntity;
import java.util.*;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class PathFinder {

	private static final int MAX_FALL_HEIGHT = 4;
	private static final int MAX_PATH_LENGTH = 1000;

	//	private static final double PLAYER_HEIGHT = 1.8;
	//	private static final double PLAYER_WIDTH = 0.6;
	//	private static final double JUMP_HEIGHT = 1.25;

	private final ServerWorld world;
	private final NPCEntity npc;

	public PathFinder(ServerWorld world, NPCEntity npc) {
		this.world = world;
		this.npc = npc;
	}

	public void executeMovement(List<PathNode> path) {
		if (path == null || path.isEmpty()) {
			return;
		}

		for (int i = 1; i < path.size(); i++) {
			PathNode node = path.get(i);
			Movement move = node.movement;

			switch (move.type()) {
				case WALK:
					walkTo(node.pos);
					break;
				case JUMP:
					jumpTo(node.pos);
					break;
				case FALL:
					fallTo(node.pos);
					break;
				case SWIM:
					swimTo(node.pos);
					break;
			}
		}
	}

	public List<PathNode> findPath(BlockPos start, BlockPos target) {
		PriorityQueue<PathNode> openSet = new PriorityQueue<>();
		Set<BlockPos> closedSet = new HashSet<>();
		Map<BlockPos, PathNode> allNodes = new HashMap<>();

		PathNode startNode = new PathNode(start, null);
		startNode.costFromStart = 0;
		startNode.estimatedTotalCost = heuristic(start, target);

		openSet.add(startNode);
		allNodes.put(start, startNode);

		int iterations = 0;

		while (!openSet.isEmpty() && iterations < MAX_PATH_LENGTH) {
			iterations++;
			PathNode current = openSet.poll();

			if (current.pos.equals(target)) {
				return reconstructPath(current);
			}

			closedSet.add(current.pos);

			for (Movement move : getPossibleMoves(current.pos)) {
				BlockPos neighborPos = move.destination();

				if (closedSet.contains(neighborPos)) {
					continue;
				}

				double newG = current.costFromStart + move.cost();

				PathNode neighbor = allNodes.getOrDefault(neighborPos, new PathNode(neighborPos, current));

				if (!allNodes.containsKey(neighborPos) || newG < neighbor.costFromStart) {
					neighbor.parent = current;
					neighbor.costFromStart = newG;
					neighbor.estimatedTotalCost = newG + heuristic(neighborPos, target);
					neighbor.movement = move;

					allNodes.put(neighborPos, neighbor);

					if (!openSet.contains(neighbor)) {
						openSet.add(neighbor);
					}
				}
			}
		}

		return null;
	}

	private List<PathNode> reconstructPath(PathNode targetNode) {
		List<PathNode> path = new ArrayList<>();
		PathNode current = targetNode;

		while (current != null) {
			path.add(current);
			current = current.parent;
		}

		Collections.reverse(path);
		return path;
	}

	private List<Movement> getPossibleMoves(BlockPos pos) {
		List<Movement> moves = new ArrayList<>();

		// Check all neighboring positions
		for (int x = -1; x <= 1; x++) {
			for (int z = -1; z <= 1; z++) {
				if (x == 0 && z == 0) continue;

				addWalkingMoves(pos, x, z, moves);

				addJumpingMoves(pos, x, z, moves);

				addFallingMoves(pos, x, z, moves);
			}
		}

		if (isWater(pos)) {
			addSwimmingMoves(pos, moves);
		}

		return moves;
	}

	private void addWalkingMoves(BlockPos pos, int x, int z, List<Movement> moves) {
		BlockPos newPos = pos.add(x, 0, z);

		// Check if we can walk to this position
		if (isWalkable(newPos) && hasHeadSpace(newPos)) {
			double cost = Math.sqrt(x * x + z * z); // Diagonal moves cost more
			moves.add(new Movement(MovementType.WALK, newPos, cost));
		}
	}

	private void addJumpingMoves(BlockPos pos, int x, int z, List<Movement> moves) {
		BlockPos jumpPos = pos.add(x, 1, z);

		// Check if we can jump to this position
		if (isWalkable(jumpPos) && hasHeadSpace(jumpPos)) {
			// Check if we need to jump (block in the way or gap)
			BlockPos currentLevel = pos.add(x, 0, z);
			if (!isWalkable(currentLevel) || hasNoGroundBelow(currentLevel)) {
				double cost = 1.5 * Math.sqrt(x * x + z * z); // Jumping costs more
				moves.add(new Movement(MovementType.JUMP, jumpPos, cost));
			}
		}
	}

	private void addFallingMoves(BlockPos pos, int x, int z, List<Movement> moves) {
		for (int y = 1; y <= MAX_FALL_HEIGHT; y++) {
			BlockPos fallPos = pos.add(x, -y, z);

			// Check if we can fall to this position
			if (isWalkable(fallPos) && hasHeadSpace(fallPos)) {
				if (hasNoGroundBelow(pos.add(x, -(y - 1), z))) {
					double cost = y * 0.5; // Falling has reduced cost
					moves.add(new Movement(MovementType.FALL, fallPos, cost));
				}
				break; // Stop checking further down once we hit a valid position
			}
		}
	}

	private void addSwimmingMoves(BlockPos pos, List<Movement> moves) {
		for (int x = -1; x <= 1; x++) {
			for (int y = -1; y <= 1; y++) {
				for (int z = -1; z <= 1; z++) {
					if (x == 0 && y == 0 && z == 0) continue;

					BlockPos swimPos = pos.add(x, y, z);
					if (isWater(swimPos)) {
						double cost = Math.sqrt(x * x + y * y + z * z) * 1.3;
						moves.add(new Movement(MovementType.SWIM, swimPos, cost));
					}
				}
			}
		}
	}

	private boolean isWalkable(BlockPos pos) {
		BlockState state = world.getBlockState(pos.down());
		return !state.isAir()
				&& state.getBlock() != Blocks.WATER
				&& world.getBlockState(pos).isAir();
	}

	private boolean hasHeadSpace(BlockPos pos) {
		return world.getBlockState(pos.up()).isAir();
	}

	private boolean hasNoGroundBelow(BlockPos pos) {
		return world.getBlockState(pos.down()).isAir();
	}

	private boolean isWater(BlockPos pos) {
		return world.getBlockState(pos).getBlock() == Blocks.WATER;
	}

	private double heuristic(BlockPos start, BlockPos target) {
		// Manhattan distance for horizontal movement
		double dx = Math.abs(start.getX() - target.getX());
		double dz = Math.abs(start.getZ() - target.getZ());
		double horizontal = dx + dz;

		// Vertical distance with penalty
		double dy = Math.abs(start.getY() - target.getY());
		double vertical = dy * 2; // Vertical movement is more costly

		return horizontal + vertical;
	}

	private void walkTo(BlockPos target) {
		Vec3d targetVec = new Vec3d(target.getX() + 0.5, target.getY(), target.getZ() + 0.5);
		moveTowards(targetVec, 0.2);
	}

	private void jumpTo(BlockPos target) {
		Vec3d targetVec = new Vec3d(target.getX() + 0.5, target.getY(), target.getZ() + 0.5);
		npc.setVelocity(npc.getVelocity().add(0, 0.42, 0)); // Minecraft jump velocity
		moveTowards(targetVec, 0.2);
	}

	private void fallTo(BlockPos target) {
		Vec3d targetVec = new Vec3d(target.getX() + 0.5, target.getY(), target.getZ() + 0.5);
		moveTowards(targetVec, 0.1);
	}

	private void swimTo(BlockPos target) {
		Vec3d targetVec = new Vec3d(target.getX() + 0.5, target.getY() + 0.5, target.getZ() + 0.5);
		moveTowards(targetVec, 0.15);
	}

	private void moveTowards(Vec3d target, double speed) {
		Vec3d pos = npc.getPos();
		Vec3d direction = target.subtract(pos).normalize();
		npc.move(net.minecraft.entity.MovementType.PLAYER, direction.multiply(speed));
	}
}
