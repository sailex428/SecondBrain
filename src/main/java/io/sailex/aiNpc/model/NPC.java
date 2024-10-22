package io.sailex.aiNpc.model;

import io.sailex.aiNpc.npc.NPCController;
import io.sailex.aiNpc.npc.NPCEntity;
import io.sailex.aiNpc.pathfinding.PathFinder;
import io.sailex.aiNpc.service.ILLMService;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class NPC {

	private UUID id;
	private NPCEntity npcEntity;
	private NPCController npcController;
	private ILLMService llmService;
	private PathFinder pathFinder;
}
