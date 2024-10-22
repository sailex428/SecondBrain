package io.sailex.aiNpc.model.command;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class NPCCommand {

	private String name;
	private NPCState npcState;
}
