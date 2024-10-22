package io.sailex.aiNpc.model.event;

import io.sailex.aiNpc.network.RequestType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InstructionMessageEvent extends NPCEvent {

	private String instruction;

	public InstructionMessageEvent(String instruction) {
		super(RequestType.INSTRUCTION);
		this.instruction = instruction;
	}
}
