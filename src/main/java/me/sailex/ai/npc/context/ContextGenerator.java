package me.sailex.ai.npc.context;

import static me.sailex.ai.npc.util.WorldUtil.getMiningLevel;
import static me.sailex.ai.npc.util.WorldUtil.getToolNeeded;

import me.sailex.ai.npc.model.context.WorldContext;
import me.sailex.ai.npc.model.database.Block;
import java.util.*;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Generates the context for the LLM based on the players world state.
 */
public class ContextGenerator {

	private static final Logger LOGGER = LogManager.getLogger(ContextGenerator.class);
	private static final int CHUNK_SCAN_RADIUS = 2;
	private static final int VERTICAL_SCAN_RANGE = 16;
	private static final int ENTITY_SCAN_RADIUS = 24;

	/**
	 * Creates a world context of entities, blocks, and inventory for the NPC.
	 *
	 * @return the context of the NPC world
	 */
	public static WorldContext getContext(ServerPlayerEntity npcEntity) {
		try {
			return new WorldContext(getNpcState(npcEntity), scanNearbyBlocks(npcEntity), scanNearbyEntities(npcEntity), getInventoryState(npcEntity));
		} catch (Exception e) {
			LOGGER.error("Exception during context generation", e);
			return null;
		}
	}

	/**
	 * Searches for block by id in the world and create BlockData
	 *
	 * @param blocks 	 relevant blocks from db
	 * @return blockData list of relevant blockData (with position)
	 */
	public static List<WorldContext.BlockData> getRelevantBlockData(
			List<Block> blocks, List<WorldContext.BlockData> contextBlocks) {
		return contextBlocks.stream()
				.filter(blockData -> blocks.stream()
						.anyMatch(block -> blockData.type().replaceAll(" ", "_").equals(block.getId())))
				.toList();
	}

	private static WorldContext.NPCState getNpcState(ServerPlayerEntity npcEntity) {
		return new WorldContext.NPCState(
				new WorldContext.Position((int) npcEntity.getX(), (int) npcEntity.getY(), (int) npcEntity.getZ()),
				npcEntity.getHealth(),
				npcEntity.getHungerManager().getFoodLevel(),
				npcEntity.isOnGround(),
				npcEntity.isTouchingWater(),
				getBiome(npcEntity));
	}

	private static String getBiome(ServerPlayerEntity npcEntity) {
		Optional<RegistryKey<Biome>> biomeKey =
				npcEntity.getWorld().getBiome(npcEntity.getBlockPos()).getKey();
		return biomeKey.map(biomeRegistryKey -> biomeRegistryKey.getValue().getPath())
				.orElse("");
	}

	private static List<WorldContext.BlockData> scanNearbyBlocks(ServerPlayerEntity npcEntity) {
		Map<String, WorldContext.BlockData> nearestBlocks = new HashMap<>();
		ChunkPos npcChunk = npcEntity.getChunkPos();

		for (int chunkX = -CHUNK_SCAN_RADIUS; chunkX <= CHUNK_SCAN_RADIUS; chunkX++) {
			for (int chunkZ = -CHUNK_SCAN_RADIUS; chunkZ <= CHUNK_SCAN_RADIUS; chunkZ++) {
				scanChunk(new ChunkPos(npcChunk.x + chunkX, npcChunk.z + chunkZ), nearestBlocks, npcEntity);
			}
		}
		return new ArrayList<>(nearestBlocks.values());
	}

	private static void scanChunk(ChunkPos chunk, Map<String, WorldContext.BlockData> nearestBlocks, ServerPlayerEntity npcEntity) {
		World world = npcEntity.getWorld();
		BlockPos.Mutable pos = new BlockPos.Mutable();
		int baseY = Math.max(0, npcEntity.getBlockPos().getY() - VERTICAL_SCAN_RANGE);
		int maxY = Math.min(world.getHeight(), npcEntity.getBlockPos().getY() + VERTICAL_SCAN_RANGE);

		for (int x = 0; x < 16; x++) {
			for (int z = 0; z < 16; z++) {
				for (int y = baseY; y < maxY; y++) {
					pos.set(chunk.getStartX() + x, y, chunk.getStartZ() + z);
					if (isAccessible(pos, world)) {
						BlockState blockState = world.getBlockState(pos);
						String blockType =
								blockState.getBlock().getName().getString().toLowerCase();
						if (blockType.contains("air")) continue;
						WorldContext.BlockData currentBlockData = buildBlockData(blockType, blockState, pos);

						if (!nearestBlocks.containsKey(blockType)
								|| isCloser(pos, nearestBlocks.get(blockType).position(), npcEntity)) {
							nearestBlocks.put(blockType, currentBlockData);
						}
					}
				}
			}
		}
	}

	private static boolean isAccessible(BlockPos pos, World world) {
		for (Direction dir : Direction.values()) {
			if (world.getBlockState(pos.offset(dir)).isAir()) {
				return true;
			}
		}
		return false;
	}

	private static boolean isCloser(BlockPos pos, WorldContext.Position otherPos, ServerPlayerEntity npcEntity) {
		double currentDistance = npcEntity.getBlockPos().getSquaredDistance(pos);
		double otherDistance =
				npcEntity.getBlockPos().getSquaredDistance(new BlockPos(otherPos.x(), otherPos.y(), otherPos.y()));
		return currentDistance < otherDistance;
	}

	private static List<WorldContext.EntityData> scanNearbyEntities(ServerPlayerEntity npcEntity) {
		return npcEntity.getWorld()
				.getOtherEntities(npcEntity, npcEntity.getBoundingBox().expand(ENTITY_SCAN_RADIUS), entity -> true)
				.stream()
				.map(entity -> new WorldContext.EntityData(
						String.valueOf(entity.getId()),
						entity.getName().getString(),
						new WorldContext.Position((int) entity.getX(), (int) entity.getY(), (int) entity.getZ()),
						entity.canHit()))
				.toList();
	}

	private static WorldContext.InventoryState getInventoryState(ServerPlayerEntity npcEntity) {
		PlayerInventory inventory = npcEntity.getInventory();

		return new WorldContext.InventoryState(
				// Hotbar
				getInventoryItemsInRange(inventory, 0, 9),
				// Main inventory
				getInventoryItemsInRange(inventory, 9, 36),
				getArmorItems(inventory),
				getInventoryItemsInHand(inventory));
	}

	private static List<WorldContext.ItemData> getInventoryItemsInHand(PlayerInventory inventory) {
		List<WorldContext.ItemData> items = new ArrayList<>();
		ItemStack stack = inventory.getMainHandStack();

		addItemData(stack, items, -1);
		return items;
	}

	private static List<WorldContext.ItemData> getInventoryItemsInRange(PlayerInventory inventory, int start, int end) {
		List<WorldContext.ItemData> items = new ArrayList<>();

		for (int i = start; i < end; i++) {
			ItemStack stack = inventory.getStack(i);
			addItemData(stack, items, i);
		}
		return items;
	}

	private static List<WorldContext.ItemData> getArmorItems(PlayerInventory inventory) {
		List<WorldContext.ItemData> items = new ArrayList<>();

		for (ItemStack stack : inventory.armor) {
			addItemData(stack, items, -1);
		}
		return items;
	}

	private static void addItemData(ItemStack stack, List<WorldContext.ItemData> items, int slot) {
		if (!stack.isEmpty()) {
			items.add(new WorldContext.ItemData(getBlockName(stack), stack.getCount(), stack.getDamage(), slot));
		}
	}

	private static String getBlockName(ItemStack stack) {
		String translationKey = stack.getItem().getTranslationKey();
		return translationKey.substring(translationKey.lastIndexOf(".") + 1);
	}

	private static WorldContext.BlockData buildBlockData(String blockType, BlockState blockState, BlockPos pos) {
		return new WorldContext.BlockData(
				blockType,
				new WorldContext.Position(pos.getX(), pos.getY(), pos.getZ()),
				getMiningLevel(blockState),
				getToolNeeded(blockState));
	}
}
