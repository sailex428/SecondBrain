package io.sailex.aiNpc.listener;

import io.sailex.aiNpc.model.NPC;
import java.util.List;

public class EventListenerManager {

	private final List<NPC> npcList;

	public EventListenerManager(List<NPC> npcList) {
		this.npcList = npcList;
	}

	public void register() {
		MessageListener messageListener = new MessageListener(npcList);
		messageListener.register();

		//		HealthListener healthListener = new HealthListener(npcs);
	}
}
