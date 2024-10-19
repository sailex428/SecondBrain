package io.sailex.aiNpc.listener;

import io.sailex.aiNpc.npc.NPCController;
import io.sailex.aiNpc.npc.NPCEntity;
import java.util.Map;

public class ServerEventListenerManager {

	private final Map<NPCEntity, NPCController> npcEntityControllerMap;

	public ServerEventListenerManager(Map<NPCEntity, NPCController> npcEntityControllerMap) {
		this.npcEntityControllerMap = npcEntityControllerMap;
	}

	public void register() {
		MessageListener messageListener = new MessageListener(npcEntityControllerMap);
		messageListener.register();
	}
}
