package io.sailex.aiNpc.client.util;

import net.minecraft.entity.player.PlayerEntity;

public class ClientWorldUtil {

    public static PlayerEntity getClosestPlayer(PlayerEntity player) {
        return player.getWorld().getClosestPlayer(
                player.getX(), player.getY(), player.getZ(),
                10,
                (entity) -> !entity.equals(player)
        );
    }

}
