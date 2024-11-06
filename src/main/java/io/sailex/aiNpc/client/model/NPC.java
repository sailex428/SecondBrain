package io.sailex.aiNpc.client.model;

import io.sailex.aiNpc.client.llm.ILLMClient;
import io.sailex.aiNpc.client.npc.NPCContextGenerator;
import io.sailex.aiNpc.client.npc.NPCController;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.minecraft.client.network.ClientPlayerEntity;

@Data
@AllArgsConstructor
public class NPC {

	private UUID id;
	private ClientPlayerEntity npcEntity;
	private NPCController npcController;
	private NPCContextGenerator npcContextGenerator;
	private ILLMClient llmService;
}
