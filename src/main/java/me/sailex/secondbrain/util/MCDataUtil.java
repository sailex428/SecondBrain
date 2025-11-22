package me.sailex.secondbrain.util;

import me.sailex.altoclef.multiversion.EntityVer;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.biome.Biome;

import java.util.List;
import java.util.Optional;

public class MCDataUtil {

	private MCDataUtil() {}

	public static PlayerEntity getClosestPlayer(PlayerEntity player) {
		return EntityVer.getWorld(player)
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

	public static String getBiome(Entity entity) {
		Optional<RegistryKey<Biome>> biomeKey =
				EntityVer.getWorld(entity).getBiome(entity.getBlockPos()).getKey();
		return biomeKey.map(biomeRegistryKey -> biomeRegistryKey.getValue().getPath())
				.orElse("");
	}

	public static Entity getNearbyEntity(String entityType, ServerPlayerEntity npc) {
		return getNearbyEntities(npc).stream()
				.filter(entity -> entity.getType().getName().getString().equals(entityType)
						|| entity.getType().getName().getString().contains(entityType))
				.findFirst().orElse(null);
	}

	public static Entity getNearbyPlayer(String playerName, ServerPlayerEntity npc) {
		return getNearbyEntities(npc).stream()
				.filter(Entity::isPlayer)
				.filter(entity -> entity.getName().getString().equals(playerName)
						|| entity.getName().getString().contains(playerName))
				.findFirst().orElse(null);
	}

	public static List<Entity> getNearbyEntities(ServerPlayerEntity npcEntity) {
		return EntityVer.getWorld(npcEntity).getOtherEntities(
				npcEntity,
				npcEntity.getBoundingBox().expand(50),
				entity -> true
		);
	}

	public static String getBlockNameByPos(Entity entity) {
		return EntityVer.getWorld(entity).getBlockState(entity.getBlockPos()).getBlock().getName().getString().toLowerCase();
	}

}
