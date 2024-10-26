package io.sailex.aiNpc.model;

import io.sailex.aiNpc.llm.ILLMService;
import io.sailex.aiNpc.npc.NPCContextGenerator;
import io.sailex.aiNpc.npc.NPCController;
import io.sailex.aiNpc.npc.NPCEntity;
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
	private NPCContextGenerator npcContextGenerator;
}
