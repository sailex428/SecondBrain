package me.sailex.ai.npc.util;

import me.sailex.ai.npc.model.context.WorldContext;
import me.sailex.ai.npc.model.database.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Stringifies resources for better communication with the LLM.
 */
public class PromptFormatter {

	private PromptFormatter() {}

	public static String formatConversation(List<Conversation> conversations) {
		return formatList(
				conversations,
				conversation ->
						String.format("- Message: %s at %s", conversation.getMessage(), conversation.getTimestamp()));
	}

	public static String formatRecipes(List<Recipe> recipes) {
		return formatList(
				recipes,
				recipe -> String.format(
						"- Item to craft: %s, table needed: %s, needed items (recipe): %s",
						recipe.getName(), recipe.getTableNeeded(), recipe.getItemsNeeded()));
	}

	private static String formatItemsNeeded(Map<String, Integer> itemsNeeded) {
		return formatList(
				new ArrayList<>(itemsNeeded.entrySet()),
				entry -> String.format("- Item: %s, needed amount: %s", entry.getKey(), entry.getValue()));
	}

	public static String formatInventory(WorldContext.InventoryState inventory) {
		return String.format(
				"""
				- main hand: %s
				- armour: %s
				- main inventory: %s
				- hotbar: %s
				""",
				formatInventoryPart(inventory.mainHandItem()),
				formatInventoryPart(inventory.armor()),
				formatInventoryPart(inventory.mainInventory()),
				formatInventoryPart(inventory.hotbar())
		);
	}

	private static String formatInventoryPart(List<WorldContext.ItemData> items) {
		return formatList(items, Record::toString);
	}

	public static String formatNPCState(WorldContext.NPCState state) {
		WorldContext.Position position = state.position();
		return String.format(
				""" 
			- your position: %s
			- health: %s
			- hunger: %s
			- on Ground: %s
			- touching water: %s
			""",
				formatPosition(position), state.health(), state.food(), state.onGround(), state.inWater());
	}

	public static String formatBlocks(List<WorldContext.BlockData> blocks) {
		return formatList(
				blocks.stream().limit(15).toList(),
				block -> String.format(
						"- Block %s is at %s can be mined with tool %s %s",
						block.type(), formatPosition(block.position()), block.mineLevel(), block.toolNeeded()));
	}

	public static String formatEntities(List<WorldContext.EntityData> entities) {
		return formatList(
				entities,
				entity -> String.format(
						"- Entity %s with entityId: %s at %s",
						entity.name(),
						entity.id(),
						formatPosition(entity.position()))
		);
	}

	private static String formatPosition(WorldContext.Position position) {
		return String.format("coordinates: x: %s y: %s, z: %s", position.x(), position.y(), position.z());
	}

	private static <T> String formatList(List<T> list, Function<T, String> formatter) {
		return list.stream().map(formatter).collect(Collectors.joining("\n"));
	}
}
