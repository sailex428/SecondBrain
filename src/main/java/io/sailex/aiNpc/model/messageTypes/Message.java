package io.sailex.aiNpc.model.messageTypes;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Message {

	private RequestType requestType;
}
