package io.sailex.aiNpc.model.llm;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LLMResponse {

	private final ResponseType type;
}
