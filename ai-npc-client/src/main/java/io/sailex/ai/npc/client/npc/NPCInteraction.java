package io.sailex.ai.npc.client.npc;

import static io.sailex.ai.npc.client.util.ActionParser.parseActions;

import com.google.gson.*;
import io.sailex.ai.npc.client.model.context.WorldContext;
import io.sailex.ai.npc.client.model.database.*;
import io.sailex.ai.npc.client.model.interaction.Actions;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Generates prompts and parses responses for communication between the NPC and the LLM.
 */
public class NPCInteraction {

	private NPCInteraction() {}

	private static final Logger LOGGER = LogManager.getLogger(NPCInteraction.class);

	/**
	 * Builds a JSON system prompt from the context of the world.
	 *
	 * @param context the context of the world
	 * @param relevantResources resources matching the user prompt
	 * @return the system prompt
	 */
	public static String buildSystemPrompt(String context, String relevantResources) {
		return String.format(
				"""
			Context from the minecraft world: %s,
			relevant Resources: %s
			""",
				context, relevantResources);
	}

	/**
	 * Parses the response from the LLM.
	 * Casts the response to Actions.
	 *
	 * @param response the response from the LLM
	 * @return the actions generated from the LLM
	 */
	public static Actions parseResponse(String response) {
		try {
			return parseActions(response);
		} catch (JsonSyntaxException e) {
			LOGGER.error("Error parsing response: {}", e.getMessage());
			throw new JsonSyntaxException("Error parsing response: " + e.getMessage());
		}
	}

	/**
	 * Formats resources to string so it can be sent to a llm
	 *
	 * @return to string formatted resources
	 */
	public static String formatResources(
			List<ActionResource> actionResources,
			List<Recipe> recipes,
			List<Conversation> conversations,
			List<WorldContext.BlockData> blocks) {
		return String.format(
				"""
				Actions: (example) actions that you have done before:
				%s,
				Recipes: recipes for items that the player request to craft:
				%s,
				Blocks: relevant blocks data that matches the player message:
				%s,
				Conversations: previous dialogues between you and the players:
				%s
				""",
				formatActions(actionResources),
				formatRecipes(recipes),
				formatConversation(conversations),
				formatBlocks(blocks));
	}

	private static String formatConversation(List<Conversation> conversations) {
		return formatList(
				conversations,
				conversation ->
						String.format("- Messages: %s at %s", conversation.getMessage(), conversation.getTimeStamp()));
	}

	private static String formatActions(List<ActionResource> actionResources) {
		return formatList(
				actionResources,
				actionResource -> String.format(
						"- Action name: %s : %s, example Json format/content for that action: %s",
						actionResource.getName(), actionResource.getDescription(), actionResource.getExample()));
	}

	private static String formatRecipes(List<Recipe> recipes) {
		return formatList(
				recipes,
				recipe -> String.format(
						"- Requirement name: %s, %s, needed items: %s",
						recipe.getItemsNeeded(), recipe.getTableNeeded(), formatBlocksNeeded(recipe.getItemsNeeded())));
	}

	private static String formatBlocksNeeded(Map<String, Integer> blocksNeeded) {
		return formatList(
				new ArrayList<>(blocksNeeded.entrySet()),
				entry -> String.format("Block: %s, needed amount: %s", entry.getKey(), entry.getValue()));
	}

	/**
	 * Formats world context to string so it can be sent to llm
	 *
	 * @param context world context
	 * @return to string formatted world context
	 */
	public static String formatContext(WorldContext context) {
		return String.format(
				"""
			Make decisions based on:

			Available Resources:
			%s

			Current State:
			- NPC state: %s
			- Inventory: %s

			Nearest Entities:
			%s

			You should:
			1. Check if the action is possible (correct tools, resources in range)
			2. Move to nearest appropriate resource if the player request for that
			3. Inform player of your actions/intentions
			""",
				formatBlocks(context.nearbyBlocks()),
				formatNPCState(context.npcState()),
				formatInventory(context.inventory()),
				formatEntities(context.nearbyEntities()));
	}

	private static String formatInventory(WorldContext.InventoryState inventory) {
		return String.format(
				"""
				- main hand: %s
				- armour: %s
				- main inventory: %s
				- hotbar: %s
				""",
				inventory.mainHandItem(), inventory.armor(), inventory.mainInventory(), inventory.hotbar());
	}

	private static String formatNPCState(WorldContext.NPCState state) {
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

	private static String formatBlocks(List<WorldContext.BlockData> blocks) {
		return formatList(
				blocks,
				block -> String.format(
						"- Block %s is at %s can be mined with tool %s %s",
						block.type(), formatPosition(block.position()), block.mineLevel(), block.toolNeeded()));
	}

	private static String formatEntities(List<WorldContext.EntityData> entities) {
		return formatList(
				entities,
				entity -> String.format(
						"- Entity of type: %s %s, %s %s",
						entity.type(),
						entity.isPlayer() ? "is a Player" : "",
						entity.canHit() ? "this entity can hit you" : "",
						formatPosition(entity.position())));
	}

	private static String formatPosition(WorldContext.Position position) {
		return String.format("Coordinates: x: %s y: %s, z: %s", position.x(), position.y(), position.z());
	}

	private static <T> String formatList(List<T> list, Function<T, String> formatter) {
		return list.stream().map(formatter).collect(Collectors.joining("\n"));
	}
}
