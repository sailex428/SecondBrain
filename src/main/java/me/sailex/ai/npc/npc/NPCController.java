package me.sailex.ai.npc.npc;

import baritone.api.IBaritone;
import baritone.api.command.exception.CommandException;
import baritone.api.pathing.goals.GoalBlock;
import baritone.api.utils.BetterBlockPos;
import me.sailex.ai.npc.constant.Instructions;
import me.sailex.ai.npc.history.ConversationHistory;
import me.sailex.ai.npc.llm.ILLMClient;
import me.sailex.ai.npc.model.context.WorldContext;
import me.sailex.ai.npc.util.LogUtil;
import me.sailex.ai.npc.util.WorldUtil;
import java.util.concurrent.*;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.StringUtils;

/**
 * Controller for managing NPC actions and events.
 * Request the NPC events (actions in-game) and executes the actions called from the llm accordingly.
 *
 * @author sailex
 */
public class NPCController {

	private final ExecutorService executorService;
	private final BlockingQueue<Runnable> actionQueue = new LinkedBlockingQueue<>();

	private final ServerPlayerEntity npcEntity;
	private final IBaritone baritone;
	private final ILLMClient llmClient;
	private final ConversationHistory history;

	private boolean isFirstRequest = true;

	public NPCController(
			ServerPlayerEntity npcEntity,
			IBaritone baritone,
			ILLMClient llmClient,
			ConversationHistory history
	) {
		this.npcEntity = npcEntity;
		this.baritone = baritone;
		this.llmClient = llmClient;
		this.history = history;
		this.executorService = Executors.newSingleThreadExecutor();

		//start ticking + explore process
		tick();
		handleInitMessage();
	}

	/**
	 * Processes an event asynchronously by allowing call actions from the llm using the specified prompt.
	 * Saves the prompt in conversation history.
	 *
	 * @param source  source of the prompt
	 * @param prompt  prompt of a user or system e.g. chatmessage of a player
	 */
	public void onEvent(String source, String prompt) {
		CompletableFuture.runAsync(() -> {
					history.add(prompt);
					llmClient.callFunctions(source, prompt, history);
				}, executorService)
				.exceptionally(e -> {
					LogUtil.error("Unexpected error occurred handling event: " + e, true);
					return null;
				});
	}

	/**
	 * Adds an action to the Queue
	 *
	 * @param action 		action (npc capability)
	 * @param isNonBlocking	whether action should direct executed
	 */
	public void addAction(Runnable action, boolean isNonBlocking) {
		if (isNonBlocking) {
			action.run();
			return;
		}
		actionQueue.add(action);
	}

	private void takeAction() {
		if (isFirstRequest) {
			cancelBaritone();
			isFirstRequest = false;
		}
		Runnable nextAction = actionQueue.poll();
		if (nextAction != null) nextAction.run();
	}

	public void handleInitMessage() {
		onEvent(StringUtils.EMPTY, Instructions.getDefaultInstruction(npcEntity.getName().getString()));
		move(new WorldContext.Position(0, 90, 0));
	}

	public void chat(String message) {
		MinecraftServer server = npcEntity.getServer();
		if (server != null) {
			server.getPlayerManager().broadcast(Text.of(message), false);
			return;
		}
		LogUtil.error("There must be some very big issues lol.", true);
	}

	public void move(WorldContext.Position targetPosition) {
		baritone.getCustomGoalProcess()
				.setGoalAndPath(new GoalBlock(targetPosition.x(), targetPosition.y(), targetPosition.z()));
	}

	public void mine(WorldContext.Position targetPosition) {
		BetterBlockPos blockPos = new BetterBlockPos(targetPosition.x(), targetPosition.y(), targetPosition.z());
		baritone.getBuilderProcess().clearArea(blockPos, blockPos);
	}

	public void attack(int entityId) {
		Entity targetEntity = npcEntity.getWorld().getEntityById(entityId);
		if (targetEntity != null) {
			npcEntity.lookAt(EntityAnchorArgumentType.EntityAnchor.EYES, targetEntity.getEyePos());
			baritone.getCommandHelper().executeAttack();
			npcEntity.swingHand(npcEntity.getActiveHand());
		}
	}

	public void drop(int slot) {
		baritone.getCommandHelper().executeDrop(slot);
	}

	public void dropAll(int slot) {
		baritone.getCommandHelper().executeDropAll(slot); //slot
	}

	public void craftItem(String recipeId) {
		//? if <=1.20.4 {
		/*Identifier identifier = new Identifier(recipeId);*/
		//?} else {
		Identifier identifier = Identifier.of(recipeId);
		//?}
		RecipeEntry<?> recipe = npcEntity.getServer().getRecipeManager().get(identifier).orElse(null);
//		ClientPlayerInteractionManager interactionManager = client.interactionManager;
//		if (recipe != null && interactionManager != null) {
//			interactionManager.clickRecipe(npc.currentScreenHandler.syncId, recipe, false);
//		} else {
//			LOGGER.warn("Could not find recipe with id: {}", recipeId);
//		}
	}

	private void lookAtPlayer() {
		if (!actionQueue.isEmpty()) return;
		PlayerEntity closestPlayer = WorldUtil.getClosestPlayer(npcEntity);
		if (closestPlayer != null) {
			npcEntity.lookAt(EntityAnchorArgumentType.EntityAnchor.EYES, closestPlayer.getEyePos());
		}
	}

	public void cancelActions() {
		actionQueue.clear();
		cancelBaritone();
	}

	private void autoRespawn() {
		if (npcEntity.isDead()) {
			baritone.getCommandHelper().executeSpawn(npcEntity.getName().getString());
		}
	}

	private void tick() {
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			autoRespawn();
			if (!baritoneIsActive()) {
				lookAtPlayer();
				takeAction();
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
            baritone.getCommandManager().execute(npcEntity.getCommandSource(), "cancel");
        } catch (CommandException e) {
			LogUtil.error("Error executing automatone cancel command" + e, true);
        }
    }
}
