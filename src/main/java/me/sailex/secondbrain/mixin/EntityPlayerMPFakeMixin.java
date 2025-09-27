package me.sailex.secondbrain.mixin;

import carpet.patches.EntityPlayerMPFake;
import com.mojang.authlib.GameProfile;
import me.sailex.secondbrain.callback.NPCEvents;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//? if >=1.21.1 {
/*import net.minecraft.network.packet.c2s.common.SyncedClientOptions;
*///?}

@Mixin(EntityPlayerMPFake.class)
public abstract class EntityPlayerMPFakeMixin extends ServerPlayerEntity {

    protected EntityPlayerMPFakeMixin(MinecraftServer server, ServerWorld world, GameProfile profile) {
        super(server, world, profile /*? >=1.21.1 {*/ /*, SyncedClientOptions.createDefault() *//*?}*/);
    }

    @Inject(method = "onDeath", at = @At("TAIL"))
    private void onDeath(DamageSource damageSource, CallbackInfo ci) {
        NPCEvents.ON_DEATH.invoker().onNPCDeath(this);
    }
}
