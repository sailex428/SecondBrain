package io.sailex.aiNpc.listener;

import io.sailex.aiNpc.model.NPC;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

import java.util.List;

public class EventListenerManager {

	private final List<NPC> npcList;

	public EventListenerManager(List<NPC> npcList) {
		this.npcList = npcList;
	}

	public void register() {
		MessageListener messageListener = new MessageListener(npcList);
		messageListener.register();

		registerServerStoppingListener();
	}

	private void registerServerStoppingListener() {
		ServerLifecycleEvents.SERVER_STOPPING.register(server -> npcList.forEach(npc -> npc.getLlmService().stopService()));
	}

}
