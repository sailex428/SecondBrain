package io.sailex.aiNpc.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class NPC {

	private String name;
	private NPCState npcState;
}
