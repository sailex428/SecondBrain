package me.sailex.ai.npc.npc;

import com.google.gson.*;
import me.sailex.ai.npc.model.context.WorldContext;
import me.sailex.ai.npc.model.database.*;
import me.sailex.ai.npc.model.interaction.Skill;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Generates prompts and parses responses for communication between the NPC and the LLM.
 */
public class NPCInteraction {

	private NPCInteraction() {}
	private static final Gson GSON = new Gson();

	private static String formatConversation(List<Conversation> conversations) {
		return formatList(
				conversations,
				conversation ->
						String.format("- Messages: %s at %s", conversation.getMessage(), conversation.getTimeStamp()));
	}

	private static String formatSkills(List<SkillResource> skills) {
		return formatList(
				skills,
				skill -> String.format(
						"- Action name: %s, example Json format/content for that action: %s",
						skill.getName(), skill.getExample()));
	}

	private static String formatRecipes(List<Recipe> recipes) {
		return formatList(
				recipes,
				recipe -> String.format(
						"- Item to craft: %s, table needed: %s, needed items (recipe): %s",
						recipe.getName(), recipe.getTableNeeded(), formatItemsNeeded(recipe.getItemsNeeded())));
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

	public static String skillToJson(Skill skill) {
		return GSON.toJson(skill);
	}

	public static Skill parseSkill(String actions) throws JsonSyntaxException {
		return GSON.fromJson(actions, Skill.class);
	}
}
