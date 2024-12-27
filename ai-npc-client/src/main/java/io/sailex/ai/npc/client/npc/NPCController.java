package io.sailex.ai.npc.client.npc;

import static io.sailex.ai.npc.client.AiNPCClient.client;
import static io.sailex.ai.npc.client.npc.NPCInteraction.*;

import baritone.api.IBaritone;
import baritone.api.pathing.goals.GoalBlock;
import baritone.api.utils.BetterBlockPos;
import io.sailex.ai.npc.client.constant.Instructions;
import io.sailex.ai.npc.client.context.ContextGenerator;
import io.sailex.ai.npc.client.database.repositories.RepositoryFactory;
import io.sailex.ai.npc.client.llm.ILLMClient;
import io.sailex.ai.npc.client.mixin.InventoryAccessor;
import io.sailex.ai.npc.client.model.context.WorldContext;
import io.sailex.ai.npc.client.model.database.Resources;
import io.sailex.ai.npc.client.model.interaction.Action;
import io.sailex.ai.npc.client.model.interaction.ActionType;
import io.sailex.ai.npc.client.model.interaction.Skill;
import io.sailex.ai.npc.client.util.ClientWorldUtil;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
	private final ILLMClient llmClient;
	private final ContextGenerator contextGenerator;
	private final RepositoryFactory repositoryFactory;
	private final IBaritone baritone;

	public NPCController(
			ClientPlayerEntity npc,
			ILLMClient llmClient,
			ContextGenerator contextGenerator,
			RepositoryFactory repositoryFactory,
			IBaritone baritone) {
		this.npc = npc;
		this.llmClient = llmClient;
		this.contextGenerator = contextGenerator;
		this.repositoryFactory = repositoryFactory;
		this.executorService = Executors.newFixedThreadPool(3);
		this.baritone = baritone;
		onClientTick();
		handleInitMessage();
	}

	/**
	 * Handles the NPC events (actions in-game) and executes NPC actions.
	 *
	 * @param eventPrompt the NPC event
	 */
	public void handleEvent(String eventPrompt) {
		CompletableFuture.runAsync(
						() -> {
							Resources resources = repositoryFactory.getRelevantResources(eventPrompt);
							WorldContext worldContext = contextGenerator.getContext();
							String relevantResources = NPCInteraction.formatResources(
									resources.getSkillResources(),
									resources.getRequirements(),
									resources.getConversations(),
									contextGenerator.getRelevantBlockData(
											resources.getBlocks(), worldContext.nearbyBlocks()));
							String formattedContext = NPCInteraction.formatContext(worldContext);

							String systemPrompt = NPCInteraction.buildSystemPrompt(formattedContext, relevantResources);

							LOGGER.info("User prompt: {}, System prompt: {}", eventPrompt, systemPrompt);

							String generatedResponse = llmClient.generateResponse(eventPrompt, systemPrompt);
							offerActions(NPCInteraction.parseResponse(generatedResponse));
						},
						executorService)
				.exceptionally(e -> {
					LOGGER.error("Error occurred handling event", e);
					return null;
				});
	}

	private void offerActions(Skill skill) {
		skill.getActions().forEach(action -> {
			if (action.getAction().equals(ActionType.STOP)) {
				executeAction(action);
				return;
			}
			actionQueue.add(action);
		});
		saveSkill(skill);
		saveConversation(skill);
	}

	private void pollAction() {
		Action nextAction = actionQueue.poll();
		if (nextAction == null) {
			return;
		}
		if (baritone.getExploreProcess().isActive()) {
			cancelBaritone();
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
			case ATTACK -> attack(action.getTargetId());
			case STOP -> cancelActions();
			default -> LOGGER.warn("Action type not recognized in: {}", actionType);
		}
	}

	public void handleInitMessage() {
		handleEvent(Instructions.getDefaultInstruction(npc.getName().getString()));
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

	private void attack(String targetId) {
		if (targetId == null) {
			LOGGER.warn("Target id is null, cannot attack");
			return;
		}
		ClientPlayerInteractionManager interactionManager = client.interactionManager;
		if (interactionManager != null) {
			Entity targetEntity = ClientWorldUtil.getEntity(targetId, npc);
			npc.lookAt(EntityAnchorArgumentType.EntityAnchor.EYES, targetEntity.getEyePos());
			interactionManager.attackEntity(npc, targetEntity);
			npc.swingHand(npc.getActiveHand());
		}
	}

	private void dropItem(String targetItem) {
		if (targetItem == null) {
			LOGGER.warn("Target item is null, cannot drop item");
			return;
		}
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
			LOGGER.warn("Could not craft item {} cause client world is null", recipeId);
			return;
		}
		// ? if <=1.21.1 {
		// ? if <=1.20.4 {
		Identifier identifier = new Identifier(recipeId);

		// ?} else {
		/*Identifier identifier = Identifier.of(recipeId);
		 */
		// ?}
		RecipeEntry<?> recipe = client.world.getRecipeManager().get(identifier).orElse(null);
		ClientPlayerInteractionManager interactionManager = client.interactionManager;
		if (recipe != null && interactionManager != null) {
			interactionManager.clickRecipe(npc.currentScreenHandler.syncId, recipe, false);
		} else {
			LOGGER.warn("Could not find recipe with id: {}", recipeId);
		}
		// ?}
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
		cancelBaritone();
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
				|| baritone.getMineProcess().isActive();
	}

	private void cancelBaritone() {
		baritone.getCommandManager().execute("cancel");
	}

	private void saveConversation(Skill skill) {
		String message = skill.getActions().stream()
				.filter(action -> action.getAction().equals(ActionType.CHAT))
				.map(Action::getMessage)
				.collect(Collectors.joining("; "));
		repositoryFactory
				.getConversationRepository()
				.insert(npc.getName().getString(), message, llmClient.generateEmbedding(List.of(message)));
	}

	private void saveSkill(Skill skill) {
		String skillJson = skillToJson(skill);
		repositoryFactory
				.getSkillRepository()
				.insert(skill.getSkillName(), skillJson, llmClient.generateEmbedding(List.of(skillJson)));
	}
}
