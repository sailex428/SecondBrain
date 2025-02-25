package me.sailex.ai.npc.mixin;

import me.sailex.ai.npc.callback.PlayerDamageCallback;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerEntity.class)
public abstract class PlayerDamageMixin extends Entity {

	PlayerDamageMixin(EntityType<?> type, World world) {
		super(type, world);
	}

	@Inject(at = @At("HEAD"), method = "damage")
	public void onDamage(DamageSource damageSource, float amount, CallbackInfoReturnable<Boolean> info) {
		ActionResult result = PlayerDamageCallback.EVENT
				.invoker()
				.interact(damageSource, amount);

		if (result == ActionResult.FAIL) {
			info.cancel();
		}
	}
}
