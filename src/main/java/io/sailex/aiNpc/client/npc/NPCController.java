package io.sailex.aiNpc.client.npc;

import baritone.api.BaritoneAPI;
import baritone.api.IBaritone;
import baritone.api.pathing.goals.GoalBlock;
import io.sailex.aiNpc.client.constant.Instructions;
import io.sailex.aiNpc.client.llm.ILLMClient;
import io.sailex.aiNpc.client.model.NPCEvent;
import io.sailex.aiNpc.client.model.context.WorldContext;
import io.sailex.aiNpc.client.model.interaction.ActionType;
import io.sailex.aiNpc.client.model.interaction.Actions;
import io.sailex.aiNpc.client.util.LogUtil;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import net.minecraft.client.network.ClientPlayerEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NPCController {

	private static final Logger LOGGER = LogManager.getLogger(NPCController.class);
	private final ExecutorService executorService;

	private final ClientPlayerEntity npc;
	private final ILLMClient llmService;
	private final NPCContextGenerator npcContextGenerator;
	private final IBaritone baritone;

	public NPCController(ClientPlayerEntity npc, ILLMClient llmService, NPCContextGenerator npcContextGenerator) {
		this.npc = npc;
		this.llmService = llmService;
		this.npcContextGenerator = npcContextGenerator;
		this.executorService = Executors.newFixedThreadPool(3);
		this.baritone = setupPathFinding();
		handleInitMessage();
	}

	private IBaritone setupPathFinding() {
		BaritoneAPI.getSettings().allowSprint.value = true;
		BaritoneAPI.getSettings().primaryTimeoutMS.value = 2000L;
		BaritoneAPI.getSettings().allowInventory.value = true;
		return BaritoneAPI.getProvider().getPrimaryBaritone();
	}

	private void handleInitMessage() {
		handleEvent(new NPCEvent(
				ActionType.CHAT,
				Instructions.getDefaultInstruction(npc.getName().getString())));
	}

	public void handleEvent(NPCEvent prompt) {
		executorService.submit(() -> {
			ActionType actionType = prompt.type();

			boolean isValidRequestType = Arrays.asList(ActionType.values()).contains(actionType);
			if (!isValidRequestType) {
				LOGGER.error("Action type not recognized: {}", actionType);
				return;
			}
			String context = NPCInteraction.formatContext(npcContextGenerator.getContext());

			String userPrompt = NPCInteraction.buildUserPrompt(prompt);
			String systemPrompt = NPCInteraction.buildSystemPrompt(context);
			llmService
					.generateResponse(userPrompt, systemPrompt)
					.thenAccept(this::handleActions)
					.exceptionally(throwable -> {
						LogUtil.error(throwable.getMessage());
						return null;
					});
		});
	}

	private void handleActions(String response) {
		Actions actions = NPCInteraction.parseResponse(response);

		actions.getActions().forEach(action -> {
			ActionType actionType = action.getAction();
			switch (actionType) {
				case CHAT -> chat(action.getMessage());
				case MOVE -> move(action.getTargetPosition());
				case MINE -> mine(action.getTargetId());
				default -> LOGGER.error("Action type not recognized in: {}", actionType);
			}
		});
	}

	private void chat(String message) {
		npc.networkHandler.sendChatMessage(message);
	}

	private void move(WorldContext.Position targetPosition) {
		baritone.getCustomGoalProcess()
				.setGoalAndPath(new GoalBlock(targetPosition.x(), targetPosition.y(), targetPosition.z()));
	}

	private void mine(String blockToMine) {
		baritone.getMineProcess().mineByName(blockToMine);
	}
}
