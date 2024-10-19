package io.sailex.aiNpc.npc;

import io.sailex.aiNpc.constant.Instructions;
import io.sailex.aiNpc.model.messageTypes.ChatMessage;
import io.sailex.aiNpc.model.messageTypes.InstructionMessage;
import io.sailex.aiNpc.service.ILLMService;
import io.sailex.aiNpc.util.FeedbackLogger;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NPCController {

	private static final Logger LOGGER = LogManager.getLogger(NPCController.class);

	private final NPCEntity npc;
	private final MinecraftServer server;
	private final ILLMService llmService;

	public NPCController(MinecraftServer server, NPCEntity npc, ILLMService llmService) {
		this.server = server;
		this.npc = npc;
		this.llmService = llmService;
		handleInstruction(new InstructionMessage(Instructions.DEFAULT_INSTRUCTION));
	}

	public void handleInstruction(InstructionMessage instructionMessage) {
		String instructionRequest = NPCInteraction.buildRequest(instructionMessage);

		llmService
				.generateResponse(instructionRequest)
				.thenAccept(response -> {
					InstructionMessage llmAnswer = (InstructionMessage) NPCInteraction.parseResponse(response);
					npc.sendMessage(Text.of(llmAnswer.getInstruction()));
				})
				.exceptionally(throwable -> {
					handleErrors(throwable.getCause());
					return null;
				});
	}

	public void handleChatMessage(ChatMessage message) {
		String messageRequest = NPCInteraction.buildRequest(message);

		llmService
				.generateResponse(messageRequest)
				.thenAccept(response -> {
					ChatMessage llmAnswer = (ChatMessage) NPCInteraction.parseResponse(response);
					npc.sendMessage(Text.of(llmAnswer.getMessage()));
				})
				.exceptionally(throwable -> {
					handleErrors(throwable.getCause());
					return null;
				});
	}

	private void handleErrors(Throwable e) {
		LOGGER.error(e.getMessage());
		server.getPlayerManager()
				.getPlayerList()
				.forEach(player -> player.sendMessage(
						FeedbackLogger.logError(e.getMessage()).get()));
	}
}
