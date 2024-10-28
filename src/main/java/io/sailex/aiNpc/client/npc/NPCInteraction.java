package io.sailex.aiNpc.client.npc;

import com.google.gson.*;
import io.sailex.aiNpc.client.constant.Instructions;
import io.sailex.aiNpc.client.constant.ResponseSchema;
import io.sailex.aiNpc.client.model.NPCEvent;
import io.sailex.aiNpc.client.model.context.WorldContext;
import io.sailex.aiNpc.client.model.interaction.Actions;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NPCInteraction {

	private static final Gson GSON = new Gson();
	private static final Logger LOGGER = LogManager.getLogger(NPCInteraction.class);

	public static String buildUserPrompt(NPCEvent message) {
//		handling for ollama
//		JsonObject request = new JsonObject();
//
//		JsonArray dataArray = new JsonArray();
//		dataArray.add(GSON.toJsonTree(message));
//
//		request.add("data", dataArray);
//		request.add("schema", GSON.toJsonTree(ResponseSchema.ALL_SCHEMAS));
//		request.add("instruction", GSON.toJsonTree(Instructions.STRUCTURE_INSTRUCTIONS));
//
//		LOGGER.info("Built request with content: {}", request);
		return GSON.toJson(message);
	}

	public static String buildSystemPrompt(String context) {
		return String.format("""
			context from the minecraft world: %s,
			""", context);
	}

	public static Actions parseResponse(String response) {
		try {
			return GSON.fromJson(response, Actions.class);
		} catch (JsonSyntaxException e) {
			LOGGER.error("Error parsing response: {}", e.getMessage());
			throw new JsonSyntaxException("Error parsing response: " + e.getMessage());
		}
	}

	public static String formatContext(WorldContext context) {
		return String.format(
				"""
			Make decisions based on:

			Available Resources:
			%s

			Current State:
			- NPC state: %s
			- Inventory: %s

			You should:
			1. Check if the action is possible (correct tools, resources in range)
			2. Move to nearest appropriate resource if needed
			3. Inform player of your actions/intentions
			""",
				formatResources(context.nearbyBlocks()),
				formatNPCState(context.npcState()),
				formatInventory(context.inventory()));
	}

	public static String formatInventory(WorldContext.InventoryState inventory) {
		return String.format(
				"""
				- main hand: %s
				- armour: %s
				- main inventory: %s
				- hotbar: %s
				""",
				inventory.mainHandItem(), inventory.armor(), inventory.mainInventory(), inventory.hotbar());
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
				String.format("x: %s y: %s, z: %s", position.x(), position.y(), position.z()),
				state.health(),
				state.food(),
				state.onGround(),
				state.inWater());
	}

	public static String formatResources(List<WorldContext.BlockData> blocks) {
		return blocks.stream()
				.map(block -> String.format("- Block %s is at %s", block.type(), block.position()))
				.collect(Collectors.joining("\n"));
	}
}
