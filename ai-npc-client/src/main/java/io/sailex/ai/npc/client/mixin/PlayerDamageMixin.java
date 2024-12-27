package io.sailex.ai.npc.client.mixin;

import io.sailex.ai.npc.client.callback.PlayerDamageCallback;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.util.ActionResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class PlayerDamageMixin {

	@Inject(at = @At("HEAD"), method = "onDamaged", cancellable = true)
	public void onDamage(DamageSource damageSource, CallbackInfo info) {
		ActionResult result = PlayerDamageCallback.EVENT
				.invoker()
				.interact(damageSource.getAttacker(), damageSource.getName(), damageSource.getPosition());

		if (result == ActionResult.FAIL) {
			info.cancel();
		}
	}
}
