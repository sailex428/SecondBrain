package me.sailex.secondbrain.util;

import me.sailex.secondbrain.constant.Instructions;
import me.sailex.secondbrain.llm.function_calling.model.ChatMessage;
import me.sailex.secondbrain.model.context.*;
import me.sailex.secondbrain.model.database.Conversation;
import me.sailex.secondbrain.model.database.Recipe;
import net.minecraft.util.math.BlockPos;

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

	public static String format(String prompt, WorldContext worldContext, List<ChatMessage> conversations) {
		return Instructions.PROMPT_TEMPLATE.formatted(
				prompt,
				formatEntities(worldContext.nearbyEntities(), 10),
				formatBlocks(worldContext.nearbyBlocks(), 15),
				formatInventory(worldContext.inventory()),
				formatNPCState(worldContext.state()),
				formatConversations(conversations)
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

	private static String formatConversations(List<ChatMessage> conversations) {
		return formatList(conversations, chatMessage ->
			String.format("%s: %s", chatMessage.role(), chatMessage.content())
		);
	}

	private static String formatItemsNeeded(Map<String, Integer> itemsNeeded) {
		return formatList(
				new ArrayList<>(itemsNeeded.entrySet()),
				entry -> String.format("- Item: %s, needed amount: %s", entry.getKey(), entry.getValue()));
	}

	private static String formatPosition(BlockPos position) {
		return String.format("x: %s y: %s, z: %s", position.getX(), position.getY(), position.getZ());
	}

	private static <T> String formatList(List<T> list, Function<T, String> formatter) {
		return list.stream().map(formatter).collect(Collectors.joining(", "));
	}
}
