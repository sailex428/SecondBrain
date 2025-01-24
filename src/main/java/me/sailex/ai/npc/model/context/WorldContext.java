package me.sailex.ai.npc.model.context;

import java.util.List;

/**
 * Represents the context of the minecraft world around the NPC
 */
public record WorldContext(
		WorldContext.NPCState npcState,
		List<BlockData> nearbyBlocks,
		List<EntityData> nearbyEntities,
		WorldContext.InventoryState inventory) {

	public record Position(int x, int y, int z) {}

	public record NPCState(
			Position position, float health, int food, boolean onGround, boolean inWater, String biome) {}

	public record BlockData(String type, Position position, String mineLevel, String toolNeeded) {}

	public record EntityData(String id, String name, Position position) {}

	public record ItemData(String type, int count, int damage, int slot) {}

	public record InventoryState(
			List<ItemData> hotbar, List<ItemData> mainInventory, List<ItemData> armor, List<ItemData> mainHandItem) {}
}
