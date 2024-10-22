package io.sailex.aiNpc.model.event;

import io.sailex.aiNpc.network.RequestType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class NPCEvent {

	private final RequestType type;
}
