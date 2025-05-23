package me.sailex.secondbrain.common;

import carpet.patches.EntityPlayerMPFake;
import me.sailex.secondbrain.exception.NPCCreationException;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;

import java.util.concurrent.CountDownLatch;

public class NPCSpawner {

    private NPCSpawner() {}

    public static void spawn(String name, ServerPlayerEntity player, boolean useSpawnPos) {
        World world = player.getWorld();
        RegistryKey<World> dimensionKey = world.getRegistryKey();

        Vec3d spawnPos = useSpawnPos ? world.getSpawnPos().toCenterPos() : player.getPos();

        boolean isSuccessful = EntityPlayerMPFake.createFake(name, player.getServer(),
                spawnPos, player.getYaw(), player.getPitch(),
                dimensionKey, GameMode.SURVIVAL, false);
        if (!isSuccessful) {
            throw new NPCCreationException("Player profile doesn't exist!");
        }
    }

    public static void remove(String name, PlayerManager playerManager) {
        ServerPlayerEntity player = playerManager.getPlayer(name);
        if (player != null) {
            playerManager.remove(player);
        }
    }

    public static void checkPlayerAvailable(String npcName, CountDownLatch latch) {
        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            if (entity instanceof ServerPlayerEntity
                    && entity.getName().getString().equals(npcName)) {
                latch.countDown();
            }
        });
    }

}
