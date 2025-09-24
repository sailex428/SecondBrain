package me.sailex.secondbrain.util;

import me.sailex.secondbrain.constant.Instructions;
import me.sailex.secondbrain.model.context.*;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Stringifies resources for better communication with the LLM.
 */
public class PromptFormatter {

	private PromptFormatter() {}

	public static String format(String prompt, WorldContext worldContext) {
		return Instructions.PROMPT_TEMPLATE.formatted(
				prompt,
				formatEntities(worldContext.nearbyEntities(), 10),
				formatBlocks(worldContext.nearbyBlocks(), 15),
				formatInventory(worldContext.inventory()),
				formatNPCState(worldContext.state())
		);
	}

	public static String formatNPCState(StateData state) {
		BlockPos position = state.position();
		return String.format(
				""" 
			- your position: %s
			- health: %s
			- hunger: %s
			- biome: %s
			""",
				formatPosition(position), state.health(), state.food(), state.biome());
	}

	public static String formatBlocks(List<BlockData> blocks, int limit) {
		return formatList(blocks.stream().limit(limit).toList(), BlockData::type);
	}

	public static String formatEntities(List<EntityData> entities, int limit) {
		return formatList(entities.stream()
				.distinct()
				.limit(limit)
				.toList(), EntityData::name);
	}

	public static String formatInventory(InventoryData inventory) {
		List<String> invParts = new ArrayList<>();
		addInventoryPart(inventory.hotbar(), "hotbar: ", invParts);
		addInventoryPart(inventory.mainInventory(), "main inventory: ", invParts);
		addInventoryPart(inventory.armor(), "armor: ", invParts);
		addInventoryPart(inventory.offHand(), "offhand: ", invParts);
		if (invParts.isEmpty()) {
			invParts.add("<no items in inventory>");
		}
		return String.join("\n", invParts);
	}

	private static void addInventoryPart(List<ItemData> items, String prefix, List<String> parts) {
		if (!items.isEmpty()) {
			parts.add(prefix + formatInventoryPart(items));
		}
	}

	private static String formatInventoryPart(List<ItemData> items) {
		return formatList(items, ItemData::type);
	}

	private static String formatPosition(BlockPos position) {
		return String.format("x: %s y: %s, z: %s", position.getX(), position.getY(), position.getZ());
	}

	private static <T> String formatList(List<T> list, Function<T, String> formatter) {
		return list.stream().map(formatter).collect(Collectors.joining(", "));
	}
}
