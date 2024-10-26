package io.sailex.aiNpc.npc;

import io.sailex.aiNpc.constant.Instructions;
import io.sailex.aiNpc.llm.ILLMService;
import io.sailex.aiNpc.model.NPCEvent;
import io.sailex.aiNpc.model.context.WorldContext;
import io.sailex.aiNpc.model.llm.ChatMessage;
import io.sailex.aiNpc.model.llm.LLMResponse;
import io.sailex.aiNpc.model.llm.Move;
import io.sailex.aiNpc.model.llm.RequestType;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NPCController {

	private static final Logger LOGGER = LogManager.getLogger(NPCController.class);
	private final ExecutorService executorService;

	private final NPCEntity npc;
	private final ILLMService llmService;
	private final NPCContextGenerator npcContextGenerator;

	public NPCController(NPCEntity npc, ILLMService llmService, NPCContextGenerator npcContextGenerator) {
		this.npc = npc;
		this.llmService = llmService;
		this.npcContextGenerator = npcContextGenerator;
		this.executorService = Executors.newFixedThreadPool(3);
	}

	public void handleMessage(NPCEvent prompt) {
		executorService.submit(() -> {
			RequestType messageType = prompt.getType();

			boolean isValidRequestType = Arrays.asList(RequestType.values()).contains(messageType);
			if (!isValidRequestType) {
				LOGGER.error("Message type not recognized: {}", messageType);
				return;
			}
			WorldContext context = npcContextGenerator.getContext();

			String request = NPCInteraction.buildRequest(prompt, context);
			llmService
					.generateResponse(request)
					.thenAccept(this::handleResponses)
					.exceptionally(throwable -> {
						LOGGER.error("Error generating response: {}", throwable.getMessage());
						npc.sendChatMessage("Error generating response" + throwable.getMessage());
						return null;
					});
		});
	}

	private void handleResponses(String response) {
		LLMResponse parsedResponse = NPCInteraction.parseResponse(response);

		switch (parsedResponse) {
			case ChatMessage chatMessage -> npc.sendChatMessage(chatMessage.getMessage());
			case Move move -> npc.moveTo(move.getX(), move.getY(), move.getZ());
			default -> LOGGER.error("Response type not recognized: {}", parsedResponse.getType());
		}
	}

	public void handleInitMessage() {
		handleMessage(new NPCEvent(
				RequestType.INSTRUCTION,
				String.format("%s, Your NPC name is %s", Instructions.DEFAULT_INSTRUCTION, npc.getNpcName())));
	}
}
