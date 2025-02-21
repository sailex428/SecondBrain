package me.sailex.ai.npc.callback;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.Entity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.Vec3d;

public interface PlayerDamageCallback {

	Event<PlayerDamageCallback> EVENT = EventFactory.createArrayBacked(
			PlayerDamageCallback.class, listeners -> (attacker, target, damageName, pos) -> {
				for (PlayerDamageCallback listener : listeners) {
					ActionResult result = listener.interact(attacker, target, damageName, pos);

					if (result != ActionResult.PASS) {
						return result;
					}
				}
				return ActionResult.PASS;
			});

	ActionResult interact(Entity attacker, Entity target, String damageName, Vec3d pos);
}
