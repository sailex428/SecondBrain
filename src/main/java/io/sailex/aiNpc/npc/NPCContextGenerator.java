package io.sailex.aiNpc.npc;

import io.sailex.aiNpc.model.context.WorldContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NPCContextGenerator {

	private static final Logger LOGGER = LogManager.getLogger(NPCContextGenerator.class);
	private final ExecutorService service;
	private static final int CHUNK_SCAN_RADIUS = 2;
	private static final int VERTICAL_SCAN_RANGE = 16;
	private static final int ENTITY_SCAN_RADIUS = 16;

	private final NPCEntity npcEntity;
	private final ServerWorld world;

	public NPCContextGenerator(NPCEntity npcEntity) {
		this.npcEntity = npcEntity;
		this.world = npcEntity.getServerWorld();
		this.service = Executors.newFixedThreadPool(1);
	}

	public WorldContext getContext() {
		try {
			return new WorldContext(getNpcState(), scanNearbyBlocks(), scanNearbyEntities(), getInventoryState());
		} catch (Exception e) {
			LOGGER.error("Exception during context generation", e);
			return null;
		}
	}

	private WorldContext.NPCState getNpcState() {
		return new WorldContext.NPCState(
				new WorldContext.Position(npcEntity.getX(), npcEntity.getY(), npcEntity.getZ()),
				npcEntity.getHealth(),
				npcEntity.getHungerManager().getFoodLevel(),
				npcEntity.isOnGround(),
				npcEntity.isTouchingWater());
	}

	private List<WorldContext.BlockData> scanNearbyBlocks() {
		Map<String, WorldContext.BlockData> nearestBlocks = new HashMap<>();
		ChunkPos npcChunk = npcEntity.getChunkPos();

		for (int chunkX = -CHUNK_SCAN_RADIUS; chunkX <= CHUNK_SCAN_RADIUS; chunkX++) {
			for (int chunkZ = -CHUNK_SCAN_RADIUS; chunkZ <= CHUNK_SCAN_RADIUS; chunkZ++) {
				scanChunk(new ChunkPos(npcChunk.x + chunkX, npcChunk.z + chunkZ), nearestBlocks);
			}
		}
		return new ArrayList<>(nearestBlocks.values());
	}

	private void scanChunk(ChunkPos chunk, Map<String, WorldContext.BlockData> nearestBlocks) {
		BlockPos.Mutable pos = new BlockPos.Mutable();
		int baseY = Math.max(0, npcEntity.getBlockPos().getY() - VERTICAL_SCAN_RANGE);
		int maxY = Math.min(world.getHeight(), npcEntity.getBlockPos().getY() + VERTICAL_SCAN_RANGE);

		for (int x = 0; x < 16; x++) {
			for (int z = 0; z < 16; z++) {
				for (int y = baseY; y < maxY; y++) {
					pos.set(chunk.getStartX() + x, y, chunk.getStartZ() + z);
					Block block = world.getBlockState(pos).getBlock();
					String blockType = block.getTranslationKey();

					if (isAccessible(pos)) {
						WorldContext.BlockData currentBlockData = new WorldContext.BlockData(
								blockType, new WorldContext.Position(pos.getX(), pos.getY(), pos.getZ()), true);

						if (!nearestBlocks.containsKey(blockType)
								|| isCloser(pos, nearestBlocks.get(blockType).position())) {
							nearestBlocks.put(blockType, currentBlockData);
						}
					}
				}
			}
		}
	}

	private boolean isAccessible(BlockPos pos) {
		for (Direction dir : Direction.values()) {
			if (world.getBlockState(pos.offset(dir)).isAir()) {
				return true;
			}
		}
		return false;
	}

	private boolean isCloser(BlockPos pos, WorldContext.Position otherPos) {
		double currentDistance = npcEntity.getBlockPos().getSquaredDistance(pos);
		double otherDistance = npcEntity
				.getBlockPos()
				.getSquaredDistance(new BlockPos((int) otherPos.x(), (int) otherPos.y(), (int) otherPos.y()));
		return currentDistance < otherDistance;
	}

	private List<WorldContext.EntityData> scanNearbyEntities() {
		return world
				.getOtherEntities(
						npcEntity,
						npcEntity.getBoundingBox().expand(ENTITY_SCAN_RADIUS),
						entity -> !(entity instanceof NPCEntity)) // ignore other npcs
				.stream()
				.map(entity -> new WorldContext.EntityData(
						entity.getType().toString(),
						new WorldContext.Position(entity.getX(), entity.getY(), entity.getZ()),
						entity.canHit(),
						entity instanceof PlayerEntity))
				.collect(Collectors.toList());
	}

	private WorldContext.InventoryState getInventoryState() {
		PlayerInventory inventory = npcEntity.getInventory();

		return new WorldContext.InventoryState(
				// Hotbar
				getInventoryItemsInRange(inventory, 0, 9),
				// Main inventory
				getInventoryItemsInRange(inventory, 9, 36),
				getArmorItems(inventory),
				// Hand item
				getInventoryItemsInHand(inventory));
	}

	private List<WorldContext.ItemData> getInventoryItemsInHand(PlayerInventory inventory) {
		List<WorldContext.ItemData> items = new ArrayList<>();
		ItemStack stack = inventory.getMainHandStack();

		if (!stack.isEmpty()) {
			items.add(new WorldContext.ItemData(
					stack.getItem().getTranslationKey(), stack.getCount(), stack.getDamage(), -1));
		}
		return items;
	}

	private List<WorldContext.ItemData> getInventoryItemsInRange(PlayerInventory inventory, int start, int end) {
		List<WorldContext.ItemData> items = new ArrayList<>();

		for (int i = start; i < end; i++) {
			ItemStack stack = inventory.getStack(i);

			if (!stack.isEmpty()) {
				items.add(new WorldContext.ItemData(
						stack.getItem().getTranslationKey(), stack.getCount(), stack.getDamage(), i));
			}
		}
		return items;
	}

	private List<WorldContext.ItemData> getArmorItems(PlayerInventory inventory) {
		List<WorldContext.ItemData> items = new ArrayList<>();

		for (ItemStack stack : inventory.armor) {

			if (!stack.isEmpty()) {
				items.add(new WorldContext.ItemData(
						stack.getItem().getTranslationKey(), stack.getCount(), stack.getDamage(), -1));
			}
		}
		return items;
	}

	public void stopService() {
		service.shutdown();
	}
}
