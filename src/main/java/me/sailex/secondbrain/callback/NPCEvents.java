package me.sailex.secondbrain.callback;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.network.ServerPlayerEntity;

public final class NPCEvents {

	public static final Event<OnNPCDeath> ON_DEATH = EventFactory.createArrayBacked(OnNPCDeath.class, listeners ->
		(npcEntity) -> {
			for (OnNPCDeath listener : listeners) {
				listener.onNPCDeath(npcEntity);
			}
	});

	@FunctionalInterface
	public interface OnNPCDeath {

		void onNPCDeath(ServerPlayerEntity player);

	}

}
