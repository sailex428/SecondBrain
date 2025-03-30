package me.sailex.secondbrain.callback;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;

public interface PlayerDamageCallback {

	Event<PlayerDamageCallback> EVENT = EventFactory.createArrayBacked(
			PlayerDamageCallback.class, listeners -> (damageSource, victim, amount) -> {
				for (PlayerDamageCallback listener : listeners) {
					ActionResult result = listener.interact(damageSource, victim, amount);

					if (result != ActionResult.PASS) {
						return result;
					}
				}
				return ActionResult.PASS;
			});

	ActionResult interact(DamageSource damageSource, PlayerEntity victim, float amount);
}
