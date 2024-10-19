package io.sailex.aiNpc.model.messageTypes;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InstructionMessage extends Message {

	private String instruction;

	public InstructionMessage(String instruction) {
		super(RequestType.INSTRUCTION);
		this.instruction = instruction;
	}
}
