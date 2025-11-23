package me.sailex.secondbrain.common;

import carpet.patches.EntityPlayerMPFake;
import carpet.patches.FakeClientConnection;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import me.sailex.secondbrain.config.NPCConfig;
import me.sailex.secondbrain.mineskin.MineSkinProxyClient;
import me.sailex.secondbrain.mineskin.MineSkinProxyClientException;
import me.sailex.secondbrain.mineskin.SkinResponse;
import me.sailex.secondbrain.mixin.PlayerEntityAccessor;
import me.sailex.secondbrain.util.LogUtil;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.packet.s2c.play.EntitySetHeadYawS2CPacket;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

//? >=1.21.10 {
/*import net.minecraft.component.type.ProfileComponent;
import net.minecraft.network.packet.s2c.play.EntityPositionSyncS2CPacket;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.network.packet.c2s.common.SyncedClientOptions;
import net.minecraft.entity.attribute.EntityAttributes;
import java.util.Set;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.mojang.authlib.properties.PropertyMap;
*///?} elif >=1.21.8 {

/*import net.minecraft.network.packet.s2c.play.EntityPositionSyncS2CPacket;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.network.packet.c2s.common.SyncedClientOptions;
import net.minecraft.entity.attribute.EntityAttributes;
import java.util.Set;
import net.minecraft.block.entity.SkullBlockEntity;
*///?} elif >=1.21.1 {
/*
import net.minecraft.block.entity.SkullBlockEntity;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.network.packet.c2s.common.SyncedClientOptions;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.network.packet.s2c.play.EntityPositionS2CPacket;
import net.minecraft.block.entity.SkullBlockEntity;
*///?} else {

import net.minecraft.block.entity.SkullBlockEntity;
import net.minecraft.network.packet.s2c.play.EntityPositionS2CPacket;
//?}

public class NPCSpawner {

    private static final String TEXTURES = "textures";
    private static final MineSkinProxyClient skinClient = new MineSkinProxyClient();

    private NPCSpawner() {}

    /**
     * Spawns a fake NPC player entity in the server world.
     * Uses uuid and npcName from the given gameProfile
     * Fetches skin of the provided player name from mojang api and uses it on the NPC.
     */
    public static void spawn(
        NPCConfig config,
        MinecraftServer server,
        BlockPos spawnPos,
        Consumer<ServerPlayerEntity> npcConsumer
    ) {
        GameProfile profile = new GameProfile(config.getUuid(), config.getNpcName());
        String skinUrl = config.getSkinUrl();
        if (!skinUrl.isEmpty()) {
            Property property = fetchSkin(config.getSkinUrl());
            if (property != null) {
                //? >=1.21.10 {
                /*Multimap<String, Property> properties = HashMultimap.create();
                properties.put(TEXTURES, property);
                spawnEntity(server, new GameProfile(config.getUuid(), config.getNpcName(), new PropertyMap(properties)), spawnPos, npcConsumer);
                *///?} else {
                profile.getProperties().put(TEXTURES, property);
                spawnEntity(server, profile, spawnPos, npcConsumer);
                //?}
                return;
            }
        }

        //? >=1.21.10 {
        /*fetchGameProfile(server, config.getNpcName()).thenAcceptAsync(p -> {
            GameProfile current = profile;
            if (p != null && p.name().equals(config.getNpcName())) {
                current = p;
            }
            spawnEntity(server, current, spawnPos, npcConsumer);
        }, server);
        *///?} else {
        
        fetchGameProfile(profile).thenAcceptAsync(p -> {
            GameProfile current = profile;
            if (p.isPresent() && p.get().getName().equals(profile.getName())) {
                current = p.get();
            }
            spawnEntity(server, current, spawnPos, npcConsumer);
        }, server);
        //?}
    }

    private static void spawnEntity(
            MinecraftServer server,
            GameProfile gameProfile,
            BlockPos spawnPos,
            Consumer<ServerPlayerEntity> npcConsumer
    ) {
        double yaw = 0;
        double pitch = 0;
        ServerWorld worldIn = server.getOverworld();
        RegistryKey<World> dimensionKey = worldIn.getRegistryKey();

        //? >=1.21.10 {
        /*EntityPlayerMPFake instance = EntityPlayerMPFake.respawnFake(server, worldIn, gameProfile, SyncedClientOptions.createDefault());
        BlockPos finalSpawnPos = spawnPos != null ? spawnPos : worldIn.getSpawnPoint().getPos();
        instance.fixStartingPosition = () -> instance.refreshPositionAndAngles(finalSpawnPos.getX(), finalSpawnPos.getY(), finalSpawnPos.getZ(), (float) yaw, (float) pitch);
        server.getPlayerManager().onPlayerConnect(new FakeClientConnection(NetworkSide.SERVERBOUND), instance, new ConnectedClientData(gameProfile, 0, instance.getClientOptions(), false));
        instance.teleport(worldIn, finalSpawnPos.getX(), finalSpawnPos.getY(), finalSpawnPos.getZ(), Set.of(), (float) yaw,
                (float) pitch, true);
        instance.setHealth(20.0F);
        instance.getAttributeInstance(EntityAttributes.STEP_HEIGHT).setBaseValue(0.6F);
        instance.interactionManager.changeGameMode(GameMode.SURVIVAL);
        server.getPlayerManager().sendToDimension(new EntitySetHeadYawS2CPacket(instance,
                (byte) (instance.headYaw * 256 / 360)), dimensionKey);
        EntityPositionSyncS2CPacket positionPacket = EntityPositionSyncS2CPacket.create(instance);
        *///?} elif >=1.21.8 {

        /*EntityPlayerMPFake instance = EntityPlayerMPFake.respawnFake(server, worldIn, gameProfile, SyncedClientOptions.createDefault());
        BlockPos finalSpawnPos = spawnPos != null ? spawnPos : worldIn.getSpawnPos();
        instance.fixStartingPosition = () -> instance.refreshPositionAndAngles(finalSpawnPos.getX(), finalSpawnPos.getY(), finalSpawnPos.getZ(), (float) yaw, (float) pitch);
        server.getPlayerManager().onPlayerConnect(new FakeClientConnection(NetworkSide.SERVERBOUND), instance, new ConnectedClientData(gameProfile, 0, instance.getClientOptions(), false));
        instance.teleport(worldIn, finalSpawnPos.getX(), finalSpawnPos.getY(), finalSpawnPos.getZ(), Set.of(), (float) yaw,
                (float) pitch, true);
        instance.setHealth(20.0F);
        instance.getAttributeInstance(EntityAttributes.STEP_HEIGHT).setBaseValue(0.6F);
        instance.interactionManager.changeGameMode(GameMode.SURVIVAL);
        server.getPlayerManager().sendToDimension(new EntitySetHeadYawS2CPacket(instance,
                (byte) (instance.headYaw * 256 / 360)), dimensionKey);
        EntityPositionSyncS2CPacket positionPacket = EntityPositionSyncS2CPacket.create(instance);

        *///?} elif >=1.21.1 {
        /*
        EntityPlayerMPFake instance = EntityPlayerMPFake.respawnFake(server, worldIn, gameProfile, SyncedClientOptions.createDefault());
        BlockPos finalSpawnPos = spawnPos != null ? spawnPos : worldIn.getSpawnPos();
        instance.fixStartingPosition = () -> instance.refreshPositionAndAngles(finalSpawnPos.getX(), finalSpawnPos.getY(), finalSpawnPos.getZ(), (float) yaw, (float) pitch);
        server.getPlayerManager().onPlayerConnect(new FakeClientConnection(NetworkSide.SERVERBOUND), instance, new ConnectedClientData(gameProfile, 0, instance.getClientOptions(), false));
        instance.teleport(worldIn, finalSpawnPos.getX(), finalSpawnPos.getY(), finalSpawnPos.getZ(), (float) yaw, (float) pitch);
        instance.setHealth(20.0F);
        instance.getAttributeInstance(EntityAttributes.GENERIC_STEP_HEIGHT).setBaseValue(0.6F);
        instance.interactionManager.changeGameMode(GameMode.SURVIVAL);
        server.getPlayerManager().sendToDimension(new EntitySetHeadYawS2CPacket(instance,
                (byte) (instance.headYaw * 256 / 360)), dimensionKey);
        EntityPositionS2CPacket positionPacket = new EntityPositionS2CPacket(instance);
        *///?} else {

        EntityPlayerMPFake instance = EntityPlayerMPFake.respawnFake(server, worldIn, gameProfile);
        BlockPos finalSpawnPos = spawnPos != null ? spawnPos : worldIn.getSpawnPos();
        instance.fixStartingPosition = () -> instance.refreshPositionAndAngles(finalSpawnPos.getX(), finalSpawnPos.getY(), finalSpawnPos.getZ(), (float) yaw, (float) pitch);
        server.getPlayerManager().onPlayerConnect(new FakeClientConnection(NetworkSide.SERVERBOUND), instance);
        instance.teleport(worldIn, finalSpawnPos.getX(), finalSpawnPos.getY(), finalSpawnPos.getZ(), (float) yaw, (float) pitch);
        instance.setHealth(20.0F);
        instance.setStepHeight(0.6F);
        instance.interactionManager.changeGameMode(GameMode.SURVIVAL);
        server.getPlayerManager().sendToDimension(new EntitySetHeadYawS2CPacket(instance,
                (byte) (instance.headYaw * 256 / 360)), dimensionKey);
        EntityPositionS2CPacket positionPacket = new EntityPositionS2CPacket(instance);
        //?}

        instance.getDataTracker().set(PlayerEntityAccessor.getPlayerModelParts(), (byte) 0x7f);
        server.getPlayerManager().sendToDimension(positionPacket, dimensionKey);
        instance.getAbilities().flying = false;
        npcConsumer.accept(instance);
    }

    //? >=1.21.10 {
    /*private static CompletableFuture<GameProfile> fetchGameProfile(MinecraftServer server, final String name) {
        final ProfileComponent resolvableProfile = ProfileComponent.ofDynamic(name);
        return resolvableProfile.resolve(server.getApiServices().profileResolver());
    }
    *///?} elif >=1.21.1 {

    /*private static CompletableFuture<Optional<GameProfile>> fetchGameProfile(final GameProfile profile) {
        return SkullBlockEntity.fetchProfileByName(profile.getName());
    }
    *///?} else {

    private static CompletableFuture<Optional<GameProfile>> fetchGameProfile(final GameProfile profile) {
        CompletableFuture<Optional<GameProfile>> future = new CompletableFuture<>();
        SkullBlockEntity.loadProperties(profile, gp -> {
            if (gp != null) {
                future.complete(Optional.of(gp));
            } else {
                future.complete(Optional.empty());
            }
        });
        return future;
    }
    //?}

    private static Property fetchSkin(String skinUrl) {
        try {
            if (skinUrl != null) {
                SkinResponse response = skinClient.getSkin(skinUrl);
                return new Property(TEXTURES, response.texture(), response.signature());
            }
        } catch (MineSkinProxyClientException e) {
            LogUtil.error(e);
        }
        return null;
    }

    public static void remove(UUID uuid, PlayerManager playerManager) {
        ServerPlayerEntity player = playerManager.getPlayer(uuid);
        if (player != null) {
            playerManager.getServer().execute(() -> playerManager.remove(player));
        }
    }

}
