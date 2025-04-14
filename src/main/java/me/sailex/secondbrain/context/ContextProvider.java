package me.sailex.secondbrain.context;

import java.util.*;

import me.sailex.secondbrain.config.BaseConfig;
import me.sailex.secondbrain.model.context.*;
import me.sailex.secondbrain.util.MCDataUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Generates the context for the LLM requests based on the NPCs world environment.
 */
public class ContextProvider {

	private final ServerPlayerEntity npcEntity;
	private final ChunkManager chunkManager;
	private WorldContext cachedContext;

	public ContextProvider(ServerPlayerEntity npcEntity, BaseConfig config) {
		this.npcEntity = npcEntity;
		this.chunkManager = new ChunkManager(npcEntity, config);
		buildContext();
	}

	/**
	 * Builds a context of the NPC entity world environment.
	 */
	public WorldContext buildContext() {
		synchronized (this) {
			WorldContext context = new WorldContext(
					getNpcState(),
					getInventoryState(),
					chunkManager.getNearbyBlocks(),
					getNearbyEntities()
			);
//			chunkManager.getNearbyBlocks().forEach(blockData -> LogUtil.debugInChat(blockData.toString()));
			this.cachedContext = context;
			return context;
		}
	}

	private StateData getNpcState() {
		return new StateData(
				npcEntity.getBlockPos(),
				npcEntity.getHealth(),
				npcEntity.getHungerManager().getFoodLevel(),
				MCDataUtil.getBiome(npcEntity));
	}

	private InventoryData getInventoryState() {
		PlayerInventory inventory = npcEntity.getInventory();
		return new InventoryData(
				// armour
				getItemsInRange(inventory, 36, 39),
				// main inventory
				getItemsInRange(inventory, 9, 35),
				// hotbar
				getItemsInRange(inventory, 0, 8),
				// off-hand
				getItemsInRange(inventory, 40, 40)
		);
	}

	private List<ItemData> getItemsInRange(PlayerInventory inventory, int start, int end) {
		List<ItemData> items = new ArrayList<>();
		for (int i = start; i <= end; i++) {
			ItemStack stack = inventory.getStack(i);
			addItemData(stack, items, i);
		}
		return items;
	}

	private void addItemData(ItemStack stack, List<ItemData> items, int slot) {
		if (!stack.isEmpty()) {
			items.add(new ItemData(getBlockName(stack), stack.getCount(), slot));
		}
	}

	private String getBlockName(ItemStack stack) {
		String translationKey = stack.getItem().getTranslationKey();
		return translationKey.substring(translationKey.lastIndexOf(".") + 1);
	}

	private List<EntityData> getNearbyEntities() {
		List<EntityData> nearbyEntities = new ArrayList<>();
		List<Entity> entities = MCDataUtil.getNearbyEntities(npcEntity);
		entities.forEach(entity ->
			nearbyEntities.add(new EntityData(entity.getId(), entity.getName().getString(), entity.isPlayer()))
		);
		return nearbyEntities.stream().toList();
	}

	public ChunkManager getChunkManager() {
		return chunkManager;
	}

	public WorldContext getCachedContext() {
		return cachedContext;
	}
}
