package me.sailex.secondbrain.common;

import carpet.patches.EntityPlayerMPFake;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;

import java.util.concurrent.CountDownLatch;

public class NPCSpawner {

    public void spawn(PlayerEntity player, String name) {
        RegistryKey<World> dimensionKey = player.getWorld().getRegistryKey();

        boolean isSuccessful = EntityPlayerMPFake.createFake(name, player.getServer(),
                player.getPos(), player.getYaw(), player.getPitch(),
                dimensionKey, GameMode.SURVIVAL, false);
        if (!isSuccessful) {
            throw new NullPointerException("Player profile doesn't exist!");
        }
    }

    public void checkPlayerAvailable(String npcName, CountDownLatch latch) {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getPlayerManager().getPlayer(npcName) != null) {
                latch.countDown();
            }
        });
    }

}
