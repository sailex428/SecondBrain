package me.sailex.ai.npc.npc;

import static me.sailex.ai.npc.npc.NPCInteraction.*;

import baritone.api.IBaritone;
import baritone.api.command.exception.CommandException;
import baritone.api.pathing.goals.GoalBlock;
import baritone.api.utils.BetterBlockPos;
import me.sailex.ai.npc.constant.Instructions;
import me.sailex.ai.npc.context.ContextGenerator;
import me.sailex.ai.npc.database.repositories.RepositoryFactory;
import me.sailex.ai.npc.llm.ILLMClient;
import me.sailex.ai.npc.model.context.WorldContext;
import me.sailex.ai.npc.model.database.Resources;
import me.sailex.ai.npc.model.interaction.Action;
import me.sailex.ai.npc.model.interaction.ActionType;
import me.sailex.ai.npc.model.interaction.Skill;
import me.sailex.ai.npc.util.WorldUtil;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
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

	private final ServerPlayerEntity npc;
	private final ILLMClient llmClient;
	private final RepositoryFactory repositoryFactory;
	private final IBaritone baritone;

	private boolean isFirstRequest = true;

	public NPCController(
			ServerPlayerEntity npc,
			ILLMClient llmClient,
			RepositoryFactory repositoryFactory,
			IBaritone baritone) {
		this.npc = npc;
		this.llmClient = llmClient;
		this.repositoryFactory = repositoryFactory;
		this.baritone = baritone;
		this.executorService = Executors.newFixedThreadPool(3);
	}

	public void start() {
		tick();
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
							WorldContext worldContext = ContextGenerator.getContext(npc);
							String relevantResources = NPCInteraction.formatResources(
									resources.getSkillResources(),
									resources.getRequirements(),
									resources.getConversations(),
									ContextGenerator.getRelevantBlockData(
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
		if (isFirstRequest) {
			cancelBaritone();
			isFirstRequest = false;
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
		move(new WorldContext.Position(0, 90, 0));
	}

	private void chat(String message) {
		MinecraftServer server = npc.getServer();
		if (server != null) {
			server.getPlayerManager().broadcast(Text.of(message), false);
			return;
		}
		LOGGER.error("There must be some big issues lol.");
	}

	private void move(WorldContext.Position targetPosition) {
		baritone.getCustomGoalProcess()
				.setGoalAndPath(new GoalBlock(targetPosition.x(), targetPosition.y(), targetPosition.z()));
	}

	private void mine(WorldContext.Position targetPosition) {
		BetterBlockPos blockPos = new BetterBlockPos(targetPosition.x(), targetPosition.y(), targetPosition.z());
		baritone.getBuilderProcess().clearArea(blockPos, blockPos);
	}

	private void attack(String targetId) {
		if (targetId == null) {
			LOGGER.warn("Target id is null, cannot attack");
			return;
		}
		Entity targetEntity = WorldUtil.getEntity(targetId, npc);
		if (targetEntity != null) {
			npc.lookAt(EntityAnchorArgumentType.EntityAnchor.EYES, targetEntity.getEyePos());
			baritone.getCommandHelper().executeAttack();
			npc.swingHand(npc.getActiveHand());
		}
	}

	private void dropItem(String slot) {
		//baritone.getCommandHelper().executeDrop(Integer.parseInt(slot));
	}

	private void craftItem(String recipeId) {
		//? if <=1.20.4 {
		/*Identifier identifier = new Identifier(recipeId);*/
		//?} else {
		Identifier identifier = Identifier.of(recipeId);
		//?}
		RecipeEntry<?> recipe = npc.getServer().getRecipeManager().get(identifier).orElse(null);
//		ClientPlayerInteractionManager interactionManager = client.interactionManager;
//		if (recipe != null && interactionManager != null) {
//			interactionManager.clickRecipe(npc.currentScreenHandler.syncId, recipe, false);
//		} else {
//			LOGGER.warn("Could not find recipe with id: {}", recipeId);
//		}
	}

	private void lookAtPlayer() {
		if (!actionQueue.isEmpty()) return;
		PlayerEntity closestPlayer = WorldUtil.getClosestPlayer(npc);
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

	private void tick() {
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			//autoRespawn();
			if (!baritoneIsActive()) {
				lookAtPlayer();
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
        try {
            baritone.getCommandManager().execute(npc.getCommandSource(), "cancel");
        } catch (CommandException e) {
			LOGGER.error("Error executing automatone cancel command", e);
        }
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
		if (skill.getSkillName() != null) {
			repositoryFactory
					.getSkillRepository()
					.insert(skill.getSkillName(), skillJson, llmClient.generateEmbedding(List.of(skillJson)));
		}
	}

	public void stopService() {
		executorService.shutdown();
	}
}
