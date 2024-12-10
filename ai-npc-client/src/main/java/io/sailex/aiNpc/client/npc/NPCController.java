package io.sailex.aiNpc.client.npc;

import baritone.api.BaritoneAPI;
import baritone.api.IBaritone;
import baritone.api.pathing.goals.GoalBlock;
import baritone.api.utils.BetterBlockPos;
import io.sailex.aiNpc.client.constant.Instructions;
import io.sailex.aiNpc.client.llm.ILLMClient;
import io.sailex.aiNpc.client.mixin.InventoryAccessor;
import io.sailex.aiNpc.client.model.NPCEvent;
import io.sailex.aiNpc.client.model.context.WorldContext;
import io.sailex.aiNpc.client.model.interaction.Action;
import io.sailex.aiNpc.client.model.interaction.ActionType;
import io.sailex.aiNpc.client.model.interaction.Actions;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

import io.sailex.aiNpc.client.util.ClientWorldUtil;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Controller for managing NPC actions and events.
 * Handles the NPC events (actions in-game) and executes the actions generated from the llm accordingly.
 */
public class NPCController {

	private static final Logger LOGGER = LogManager.getLogger(NPCController.class);
	private final ExecutorService executorService;
	private final BlockingQueue<Action> actionQueue = new LinkedBlockingQueue<>();

	private final ClientPlayerEntity npc;
	private final ILLMClient llmService;
	private final NPCContextGenerator npcContextGenerator;
	private final IBaritone baritone;

	/**
	 * Constructor for NPCController.
	 *
	 * @param npc                 the NPC entity
	 * @param llmService          the LLM client
	 * @param npcContextGenerator the NPC context generator
	 */
	public NPCController(ClientPlayerEntity npc, ILLMClient llmService, NPCContextGenerator npcContextGenerator) {
		this.npc = npc;
		this.llmService = llmService;
		this.npcContextGenerator = npcContextGenerator;
		this.executorService = Executors.newFixedThreadPool(3);
		this.baritone = setupPathFinding();
		handleInitMessage();
		onClientTick();
	}

	private IBaritone setupPathFinding() {
		BaritoneAPI.getSettings().allowSprint.value = true;
		BaritoneAPI.getSettings().primaryTimeoutMS.value = 2000L;
		BaritoneAPI.getSettings().allowInventory.value = true;
		return BaritoneAPI.getProvider().getPrimaryBaritone();
	}

	/**
	 * Handles the NPC events (actions in-game).
	 *
	 * @param prompt the NPC event
	 */
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

			LOGGER.info("User prompt: {}, System prompt: {}", userPrompt, systemPrompt);

			String generatedResponse = llmService.generateResponse(userPrompt, systemPrompt);
			Actions actions = NPCInteraction.parseResponse(generatedResponse);
			offerActions(actions);
		});
	}

	private void offerActions(Actions actions) {
		actions.getActions().forEach(action -> {
			if (action.getAction().equals(ActionType.STOP) ||
					action.getAction().equals(ActionType.CHAT)) {
				executeAction(action);
				return;
			}
			actionQueue.add(action);
		});
	}

	private void pollAction() {
		Action nextAction = actionQueue.poll(); //TODO: use take
		if (nextAction == null) {
			return;
		}
		executeAction(nextAction);
	}

	private void executeAction(Action action) {
		ActionType actionType = action.getAction();
		switch (actionType) {
			case CHAT -> chat(action.getMessage());
			case MOVE -> move(action.getTargetPosition());
			case MINE -> mine(action.getTargetPosition());
			case DROP -> dropItem(action.getTargetType());
			case STOP -> cancelActions();
			default -> LOGGER.warn("Action type not recognized in: {}", actionType);
		}
	}

	private void handleInitMessage() {
		handleEvent(new NPCEvent(
				ActionType.CHAT,
				Instructions.getDefaultInstruction(npc.getName().getString())));
		baritone.getExploreProcess().explore(20, 40);
	}

	private void chat(String message) {
		npc.networkHandler.sendChatMessage(message);
	}

	private void move(WorldContext.Position targetPosition) {
		baritone.getCustomGoalProcess()
				.setGoalAndPath(new GoalBlock(targetPosition.x(), targetPosition.y(), targetPosition.z()));
	}

	private void mine(WorldContext.Position targetPosition) {
		BetterBlockPos blockPos = new BetterBlockPos(targetPosition.x(), targetPosition.y(), targetPosition.z());
		baritone.getSelectionManager().addSelection(blockPos, blockPos);
		baritone.getBuilderProcess().clearArea(blockPos, blockPos);
	}

	private void dropItem(String targetItem) {
		if (targetItem == null) return;
		PlayerInventory npcInventory = npc.getInventory();
		List<DefaultedList<ItemStack>> inventoryItems = ((InventoryAccessor) npcInventory).getCombinedInventory();

		inventoryItems.forEach(itemStacks -> {
			for (int i = 0; i < itemStacks.size(); i++) {
				ItemStack itemStack = itemStacks.get(i);
				if (itemStack.getItem().getTranslationKey().contains(targetItem)) {
					npcInventory.selectedSlot = i;
					npc.dropSelectedItem(true);
					break;
				}
			}
		});
	}

	private void lookAtPlayer() {
		if (!actionQueue.isEmpty()) return;
		PlayerEntity closestPlayer = ClientWorldUtil.getClosestPlayer(npc);
		if (closestPlayer != null) {
			npc.lookAt(EntityAnchorArgumentType.EntityAnchor.EYES, closestPlayer.getEyePos());
		}
	}

	private void cancelActions() {
		actionQueue.clear();
		baritone.getCommandManager().execute("cancel");
	}

	private void autoRespawn() {
		if (npc.isDead()) {
			npc.requestRespawn();
		}
	}

	private void onClientTick() {
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			lookAtPlayer();
			autoRespawn();
			if (!baritoneIsActive()) {
				pollAction();
			}
		});
	}

	private boolean baritoneIsActive() {
		return baritone.getPathingBehavior().isPathing()
				|| baritone.getCustomGoalProcess().isActive()
				|| baritone.getMineProcess().isActive()
				|| baritone.getFollowProcess().isActive()
				|| baritone.getFarmProcess().isActive();
	}

}
