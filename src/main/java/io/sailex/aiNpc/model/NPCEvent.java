package io.sailex.aiNpc.model;

import io.sailex.aiNpc.model.llm.RequestType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class NPCEvent {

	private final RequestType type;
	private final String message;
}
