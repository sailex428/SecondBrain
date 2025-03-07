package me.sailex.ai.npc.mixin;

import com.mojang.authlib.GameProfile;
import me.sailex.ai.npc.callback.PlayerDamageCallback;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerEntity.class)
public abstract class PlayerDamageMixin extends PlayerEntity {

	protected PlayerDamageMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
		super(world, pos, yaw, gameProfile);
	}

	@Inject(at = @At("HEAD"), method = "damage")
	public void onDamage(DamageSource damageSource, float amount, CallbackInfoReturnable<Boolean> info) {
		ActionResult result = PlayerDamageCallback.EVENT
				.invoker()
				.interact(damageSource, this,  amount);

		if (result == ActionResult.FAIL) {
			info.cancel();
		}
	}
}
