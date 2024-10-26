package io.sailex.aiNpc.model.context;

import java.util.List;

public record WorldContext(
		WorldContext.NPCState npcState,
		List<BlockData> nearbyBlocks,
		List<EntityData> nearbyEntities,
		WorldContext.InventoryState inventory) {

	public record Position(double x, double y, double z) {}

	public record NPCState(Position position, float health, int food, boolean onGround, boolean inWater) {}

	public record BlockData(String type, Position position, boolean accessible) {}

	public record EntityData(String type, Position position, boolean canHit, boolean isPlayer) {}

	public record ItemData(String type, int count, int damage, int slot) {}

	public record InventoryState(
			List<ItemData> hotbar, List<ItemData> mainInventory, List<ItemData> armor, List<ItemData> mainHandItem) {}
}
