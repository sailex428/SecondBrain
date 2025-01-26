package me.sailex.ai.npc.util;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.tag.BlockTags;

public class WorldUtil {

	private WorldUtil() {}

	public static PlayerEntity getClosestPlayer(PlayerEntity player) {
		return player.getWorld()
				.getClosestPlayer(player.getX(), player.getY(), player.getZ(), 10, entity -> !entity.equals(player));
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
