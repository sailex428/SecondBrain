package me.sailex.secondbrain.mixin;

import carpet.patches.EntityPlayerMPFake;
import carpet.patches.NetHandlerPlayServerFake;
import com.mojang.authlib.GameProfile;
import me.sailex.secondbrain.common.NPCEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.c2s.common.SyncedClientOptions;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerManager.class)
public abstract class PlayerManagerMixin {
    @Shadow
    @Final
    private MinecraftServer server;

    @Inject(method = "load", at = @At(value = "RETURN", shift = At.Shift.BEFORE))
    private void fixStartingPos(ServerPlayerEntity serverPlayerEntity, CallbackInfoReturnable<NbtCompound> cir)
    {
        if (serverPlayerEntity instanceof NPCEntity)
        {
            ((NPCEntity) serverPlayerEntity).fixStartingPosition.run();
        }
    }

    @Redirect(method = "placeNewPlayer", at = @At(value = "NEW", target = "(Lnet/minecraft/server/MinecraftServer;Lnet/minecraft/network/Connection;Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/server/network/CommonListenerCookie;)Lnet/minecraft/server/network/ServerGamePacketListenerImpl;"))
    private ServerPlayNetworkHandler replaceNetworkHandler(MinecraftServer server, ClientConnection clientConnection, ServerPlayerEntity playerIn, ConnectedClientData cookie)
    {
        if (playerIn instanceof NPCEntity fake)
        {
            return new NetHandlerPlayServerFake(this.server, clientConnection, fake, cookie);
        }
        else
        {
            return new ServerPlayNetworkHandler(this.server, clientConnection, playerIn, cookie);
        }
    }

    @Redirect(method = "respawn", at = @At(value = "NEW", target = "(Lnet/minecraft/server/MinecraftServer;Lnet/minecraft/server/level/ServerLevel;Lcom/mojang/authlib/GameProfile;Lnet/minecraft/server/level/ClientInformation;)Lnet/minecraft/server/level/ServerPlayer;"))
    public ServerPlayerEntity makePlayerForRespawn(MinecraftServer minecraftServer, ServerWorld serverLevel, GameProfile gameProfile, SyncedClientOptions cli, ServerPlayerEntity serverPlayer, boolean i)
    {
        if (serverPlayer instanceof NPCEntity) {
            return NPCEntity.respawnFake(minecraftServer, serverLevel, gameProfile, cli);
        }
        return new ServerPlayerEntity(minecraftServer, serverLevel, gameProfile, cli);
    }
}
