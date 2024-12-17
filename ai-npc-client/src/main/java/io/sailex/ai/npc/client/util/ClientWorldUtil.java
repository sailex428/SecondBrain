package io.sailex.ai.npc.client.util;

import net.minecraft.entity.player.PlayerEntity;

public class ClientWorldUtil {

    private ClientWorldUtil() {}

    public static PlayerEntity getClosestPlayer(PlayerEntity player) {
        return player.getWorld().getClosestPlayer(
                player.getX(), player.getY(), player.getZ(),
                10,
                entity -> !entity.equals(player)
        );
    }

}
