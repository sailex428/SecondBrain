package me.sailex.ai.npc.util;

import net.minecraft.block.BlockState;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.network.ServerPlayerEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WorldUtil {

	private static final Logger LOGGER = LogManager.getLogger(WorldUtil.class);

	private WorldUtil() {}

	public static PlayerEntity getClosestPlayer(PlayerEntity player) {
		return player.getWorld()
				.getClosestPlayer(player.getX(), player.getY(), player.getZ(), 10, entity -> !entity.equals(player));
	}

	public static Entity getEntity(String targetId, ServerPlayerEntity player) {
		try {
			int id = Integer.parseInt(targetId);
			return player.getWorld().getEntityById(id);
		} catch (NumberFormatException e) {
			LOGGER.warn("Could not convert targetId: {} to int", targetId);
			return null;
		}
	}

	public static String getMiningLevel(BlockState state) {
		if (state.isIn(BlockTags.NEEDS_STONE_TOOL)) {
			return "stone";
		} else if (state.isIn(BlockTags.NEEDS_IRON_TOOL)) {
			return "iron";
		} else if (state.isIn(BlockTags.NEEDS_DIAMOND_TOOL)) {
			return "diamond";
		} else {
			return "";
		}
	}

	public static String getToolNeeded(BlockState state) {
		if (state.isIn(BlockTags.AXE_MINEABLE)) {
			return "axe";
		} else if (state.isIn(BlockTags.PICKAXE_MINEABLE)) {
			return "pickaxe";
		} else if (state.isIn(BlockTags.SHOVEL_MINEABLE)) {
			return "shovel";
		} else if (state.isIn(BlockTags.HOE_MINEABLE)) {
			return "hoe";
		} else {
			return "hand";
		}
	}
}
