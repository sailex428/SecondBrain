package io.sailex.aiNpc.npc;

import io.sailex.aiNpc.model.ResponseSchema;
import io.sailex.aiNpc.model.event.ChatMessageEvent;
import io.sailex.aiNpc.model.event.InstructionMessageEvent;
import io.sailex.aiNpc.model.event.NPCEvent;
import io.sailex.aiNpc.network.RequestType;
import io.sailex.aiNpc.service.ILLMService;
import java.util.Arrays;
import java.util.Map;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NPCController {

	private static final Logger LOGGER = LogManager.getLogger(NPCController.class);

	private final NPCEntity npc;
	private final ILLMService llmService;
	private final MinecraftServer server;

	public NPCController(MinecraftServer server, NPCEntity npc, ILLMService llmService) {
		this.server = server;
		this.npc = npc;
		this.llmService = llmService;
	}

	public void handleMessage(NPCEvent prompt, Map<String, Object> responseSchema) {
		RequestType messageType = prompt.getType();

		boolean isValidRequestType = Arrays.asList(RequestType.values()).contains(messageType);
		if (!isValidRequestType) {
			LOGGER.error("Message type not recognized: {}", messageType);
			return;
		}
		String request = NPCInteraction.buildRequest(prompt, responseSchema);
		llmService.generateResponse(request).thenAccept(this::handleResponses).exceptionally(throwable -> {
			npc.sendMessage(Text.of("Error generating response" + throwable.getMessage()));
			LOGGER.error("Error generating response: {}", throwable.getMessage());
			return null;
		});
	}

	private void handleResponses(String response) {
		NPCEvent parsedResponse = NPCInteraction.parseResponse(response);
		if (parsedResponse instanceof ChatMessageEvent) {
			npc.sendChatMessage(((ChatMessageEvent) parsedResponse).getMessage());
		}
	}

	public void handleInitMessage() {
		handleMessage(
				new InstructionMessageEvent(String.format("Your NPC name is %s", npc.getNpcName())),
				ResponseSchema.CHAT_MESSAGE);
	}
}
