package io.sailex.ai.npc.client.callback;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.Entity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.Vec3d;

public interface PlayerDamageCallback {

	Event<PlayerDamageCallback> EVENT =
			EventFactory.createArrayBacked(PlayerDamageCallback.class, (listeners) -> (attacker, damageName, pos) -> {
				for (PlayerDamageCallback listener : listeners) {
					ActionResult result = listener.interact(attacker, damageName, pos);

					if (result != ActionResult.PASS) {
						return result;
					}
				}
				return ActionResult.PASS;
			});

	ActionResult interact(Entity attacker, String damageName, Vec3d pos);
}
