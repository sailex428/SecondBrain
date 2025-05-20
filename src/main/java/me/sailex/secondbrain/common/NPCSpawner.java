package me.sailex.secondbrain.common;

import carpet.patches.EntityPlayerMPFake;
import carpet.patches.FakeClientConnection;
import com.mojang.authlib.GameProfile;
import me.sailex.secondbrain.config.NPCConfig;
import me.sailex.secondbrain.mixin.PlayerEntityAccessor;
import net.minecraft.block.entity.SkullBlockEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.packet.c2s.common.SyncedClientOptions;
import net.minecraft.network.packet.s2c.play.EntityPositionS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitySetHeadYawS2CPacket;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;

public class NPCSpawner {

    private NPCSpawner() {}

    /**
     * Spawns a fake NPC player entity in the server world.
     * Fetches skin of the provided player name and uses it on the NPC.
     */
    public static void spawn(
        NPCConfig config,
        MinecraftServer server,
        BlockPos spawnPos,
        CountDownLatch latch
    ) {
        GameProfile gameProfile = new GameProfile(config.getUuid(), config.getNpcName());

        fetchGameProfile(config.getNpcName()).thenAcceptAsync(p -> {
            GameProfile current = gameProfile;
            if (p.isPresent()) {
                current = p.get();
            }
            spawnEntity(server, current, spawnPos, latch);
        }, server);
    }

    private static void spawnEntity(
        MinecraftServer server,
        GameProfile gameProfile,
        BlockPos spawnPos,
        CountDownLatch latch
    ) {
        double yaw = 0;
        double pitch = 0;
        ServerWorld worldIn = server.getOverworld();
        RegistryKey<World> dimensionKey = worldIn.getRegistryKey();

        EntityPlayerMPFake instance = EntityPlayerMPFake.respawnFake(server, worldIn, gameProfile, SyncedClientOptions.createDefault());
        BlockPos finalSpawnPos = spawnPos != null ? spawnPos : instance.getWorldSpawnPos(worldIn,
                worldIn.getSpawnPos());
        instance.fixStartingPosition = () -> instance.refreshPositionAndAngles(finalSpawnPos.getX(), finalSpawnPos.getY(), finalSpawnPos.getZ(), (float) yaw, (float) pitch);
        server.getPlayerManager().onPlayerConnect(new FakeClientConnection(NetworkSide.SERVERBOUND), instance, new ConnectedClientData(gameProfile, 0, instance.getClientOptions(), false));
        instance.teleport(worldIn, finalSpawnPos.getX(), finalSpawnPos.getY(), finalSpawnPos.getZ(), (float) yaw,
                (float) pitch);
        instance.setHealth(20.0F);
        //((EntityAccessor) instance).unsetRemoved();
        instance.getAttributeInstance(EntityAttributes.GENERIC_STEP_HEIGHT).setBaseValue(0.6F);
        instance.interactionManager.changeGameMode(GameMode.SURVIVAL);
        server.getPlayerManager().sendToDimension(new EntitySetHeadYawS2CPacket(instance,
                (byte) (instance.headYaw * 256 / 360)), dimensionKey);
        server.getPlayerManager().sendToDimension(new EntityPositionS2CPacket(instance), dimensionKey);

        instance.getDataTracker().set(PlayerEntityAccessor.getPlayerModelParts(), (byte) 0x7f);
        instance.getAbilities().flying = false;
        latch.countDown();
    }

    private static CompletableFuture<Optional<GameProfile>> fetchGameProfile(final String name) {
        return SkullBlockEntity.fetchProfileByName(name);
    }

    public static void remove(UUID uuid, PlayerManager playerManager) {
        ServerPlayerEntity player = playerManager.getPlayer(uuid);
        if (player != null) {
            playerManager.remove(player);
        }
    }

}
