	package io.sailex.ai.npc.client.npc;

import baritone.api.BaritoneAPI;
import baritone.api.IBaritone;
import baritone.api.pathing.goals.GoalBlock;
import baritone.api.utils.BetterBlockPos;
import io.sailex.ai.npc.client.constant.Instructions;
import io.sailex.ai.npc.client.database.repository.RepositoryFactory;
import io.sailex.ai.npc.client.llm.ILLMClient;
import io.sailex.ai.npc.client.mixin.InventoryAccessor;
import io.sailex.ai.npc.client.model.NPCEvent;
import io.sailex.ai.npc.client.model.context.WorldContext;
import io.sailex.ai.npc.client.model.database.Resource;
import io.sailex.ai.npc.client.model.interaction.Action;
import io.sailex.ai.npc.client.model.interaction.ActionType;
import io.sailex.ai.npc.client.model.interaction.Actions;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;

import io.sailex.ai.npc.client.util.ClientWorldUtil;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static io.sailex.ai.npc.client.AiNPCClient.client;

/**
 * Controller for managing NPC actions and events.
 * Handles the NPC events (actions in-game) and executes the actions generated from the llm accordingly.
 *
 * @author sailex
 */
public class NPCController {

	private static final Logger LOGGER = LogManager.getLogger(NPCController.class);
	private final ExecutorService executorService;
	private final BlockingQueue<Action> actionQueue = new LinkedBlockingQueue<>();

	private final ClientPlayerEntity npc;
	private final ILLMClient llmService;
	private final NPCContextGenerator npcContextGenerator;
	private final RepositoryFactory repositoryFactory;
	private final IBaritone baritone;

	/**
	 * Constructor for NPCController.
	 *
	 * @param npc                 the NPC entity
	 * @param llmService          the LLM client
	 * @param npcContextGenerator the NPC context generator
	 */
	public NPCController(ClientPlayerEntity npc, ILLMClient llmService, NPCContextGenerator npcContextGenerator, RepositoryFactory repositoryFactory) {
		this.npc = npc;
		this.llmService = llmService;
		this.npcContextGenerator = npcContextGenerator;
        this.repositoryFactory = repositoryFactory;
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

			List<List<Resource>> resources =  repositoryFactory.getRepositories().stream()
					.map(repo -> repo.getMostRelevantResources(prompt.message()))
					.toList();
			String relevantResources = NPCInteraction.formatResources(resources);
			String context = NPCInteraction.formatContext(npcContextGenerator.getContext());

			String userPrompt = NPCInteraction.buildUserPrompt(prompt);
			String systemPrompt = NPCInteraction.buildSystemPrompt(relevantResources + context);

			LOGGER.info("User prompt: {}, System prompt: {}", userPrompt, systemPrompt);

			String generatedResponse = llmService.generateResponse(userPrompt, systemPrompt);
			offerActions(NPCInteraction.parseResponse(generatedResponse));
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
		Action nextAction = actionQueue.poll();
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
			case CRAFT -> craftItem(action.getTargetType());
			case STOP -> cancelActions();
			default -> LOGGER.warn("Action type not recognized in: {}", actionType);
		}
	}

	private void handleInitMessage() {
		handleEvent(new NPCEvent(
				ActionType.CHAT,
				Instructions.getDefaultInstruction(npc.getName().getString())));
		baritone.getCommandManager().execute("explore");
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

	private void craftItem(String recipeId) {
		if (client.world == null) {
			LOGGER.info("Could not craft item {} cause client world is null", recipeId);
			return;
		}

		//? if <1.21.2 {
		Optional<RecipeEntry<?>> recipe = client.world.getRecipeManager().get(new Identifier(recipeId));
		//TODO: craft item from recipe
		//?
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
