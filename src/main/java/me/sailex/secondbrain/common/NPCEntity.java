package me.sailex.secondbrain.common;

import com.mojang.authlib.GameProfile;
import me.sailex.secondbrain.networking.NPCClientConnection;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.SkullBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.DisconnectionInfo;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.packet.c2s.common.SyncedClientOptions;
import net.minecraft.network.packet.c2s.play.ClientStatusC2SPacket;
import net.minecraft.network.packet.s2c.play.EntityPositionS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitySetHeadYawS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerTask;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.UserCache;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;

import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class NPCEntity extends ServerPlayerEntity {

    public Runnable fixStartingPosition = () -> {};

    /**
     * Spawns an NPC entity in the world at pos of the given player
     *
     * @param username name of the npc entity that will be spawned
     * @param player
     */
    public static boolean createNpcEntity(
        String username,
        PlayerEntity player
    ) throws ExecutionException, InterruptedException, TimeoutException {
        MinecraftServer server = player.getServer();
        World worldIn = player.getWorld();
        Vec3d pos = player.getPos();

        UserCache.setUseRemote(false);
        GameProfile gameprofile = server.getUserCache().findByName(username)
                .orElse(new GameProfile(Uuids.getOfflinePlayerUuid(username), username));

        UserCache.setUseRemote(server.isDedicated() && server.isOnlineMode());
        Optional<GameProfile> fetchedGameProfile = SkullBlockEntity.fetchProfileByName(gameprofile.getName()).get(3, TimeUnit.SECONDS);
        if (fetchedGameProfile.isPresent()) {
            gameprofile = fetchedGameProfile.get();
        }
        NPCEntity instance = new NPCEntity(server, worldIn, gameprofile, SyncedClientOptions.createDefault());
        instance.fixStartingPosition = () -> instance.refreshPositionAndAngles(pos.x, pos.y, pos.z, player.getYaw(), player.getPitch());
        server.getPlayerManager().onPlayerConnect(new NPCClientConnection(NetworkSide.SERVERBOUND), instance, new ConnectedClientData(gameprofile, 0, instance.getClientOptions(), false));
        instance.teleport(worldIn, pos.x, pos.y, pos.z, player.getYaw(), player.getPitch());
        instance.setHealth(20.0F);
        instance.unsetRemoved();
        instance.getAttributeInstance(EntityAttributes.GENERIC_STEP_HEIGHT).setBaseValue(0.6F);
        instance.interactionManager.changeGameMode(GameMode.SURVIVAL);
        server.getPlayerManager().sendToDimension(new EntitySetHeadYawS2CPacket(instance, (byte) (instance.headYaw * 256 / 360)), worldIn.getRegistryKey());
        server.getPlayerManager().sendToDimension(new EntityPositionS2CPacket(instance), worldIn.getRegistryKey());
        instance.dataTracker.set(PLAYER_MODEL_PARTS, (byte) 0x7f);
        instance.getAbilities().flying = false;
        return true;
    }

    public static NPCEntity respawnFake(MinecraftServer server, ServerWorld level, GameProfile profile, SyncedClientOptions cli) {
        return new NPCEntity(server, level, profile, cli);
    }

    private NPCEntity(MinecraftServer server, ServerWorld worldIn, GameProfile profile, SyncedClientOptions cli) {
        super(server, worldIn, profile, cli);
    }

    @Override
    public void onEquipStack(final EquipmentSlot slot, final ItemStack previous, final ItemStack stack) {
        if (!isUsingItem()) super.onEquipStack(slot, previous, stack);
    }

    @Override
    public void kill() {
        kill(Text.of("Killed"));
    }

    public void kill(Text reason) {
        shakeOff();

        if (reason.getContent() instanceof TranslatableTextContent text && text.getKey().equals("multiplayer.disconnect.duplicate_login")) {
            this.networkHandler.onDisconnected(new DisconnectionInfo(reason));
        } else {
            this.server.send(new ServerTask(this.server.getTicks(), () -> {
                this.networkHandler.onDisconnected(new DisconnectionInfo(reason));
            }));
        }
    }

    @Override
    public void tick() {
        if (this.getServer().getTicks() % 10 == 0) {
            this.networkHandler.syncWithPlayerPosition();
            this.getServerWorld().getChunkManager().updatePosition(this);
        }
        try {
            super.tick();
            this.playerTick();
        } catch (NullPointerException ignored) {
            // happens with that paper port thingy - not sure what that would fix, but hey
            // the game not gonna crash violently.
        }
    }

    private void shakeOff() {
        if (getVehicle() instanceof PlayerEntity) stopRiding();
        for (Entity passenger : getPassengersDeep()) {
            if (passenger instanceof PlayerEntity) passenger.stopRiding();
        }
    }

    @Override
    public void onDeath(DamageSource cause) {
        shakeOff();
        super.onDeath(cause);
        setHealth(20);
        this.hungerManager = new HungerManager();
        kill(this.getDamageTracker().getDeathMessage());
    }

    @Override
    public String getIp() {
        return "127.0.0.1";
    }

    @Override
    public boolean allowsServerListing() {
        return true;
    }

    @Override
    protected void fall(double y, boolean onGround, BlockState state, BlockPos pos) {
        handleFall(0.0, y, 0.0, onGround);
    }

    @Override
    public Entity teleportTo(TeleportTarget serverLevel) {
        super.teleportTo(serverLevel);
        if (notInAnyWorld) {
            ClientStatusC2SPacket p = new ClientStatusC2SPacket(ClientStatusC2SPacket.Mode.PERFORM_RESPAWN);
            networkHandler.onClientStatus(p);
        }
        if (networkHandler.player.isInTeleportationState()) {
            networkHandler.player.onTeleportationDone();
        }
        return networkHandler.player;
    }
}
