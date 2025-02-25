package me.sailex.ai.npc.callback;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.util.ActionResult;

public interface PlayerDamageCallback {

	Event<PlayerDamageCallback> EVENT = EventFactory.createArrayBacked(
			PlayerDamageCallback.class, listeners -> (damageSource, amount) -> {
				for (PlayerDamageCallback listener : listeners) {
					ActionResult result = listener.interact(damageSource, amount);

					if (result != ActionResult.PASS) {
						return result;
					}
				}
				return ActionResult.PASS;
			});

	ActionResult interact(DamageSource damageSource, float amount);
}
