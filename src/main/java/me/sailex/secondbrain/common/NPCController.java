package me.sailex.secondbrain.common;

import baritone.api.IBaritone;
import baritone.api.command.exception.CommandException;
import baritone.api.pathing.goals.GoalBlock;
import baritone.api.pathing.goals.GoalRunAway;

import me.sailex.secondbrain.context.ContextProvider;
import me.sailex.secondbrain.llm.function_calling.OllamaFunctionManager;
import me.sailex.secondbrain.llm.function_calling.OpenAiFunctionManager;
import me.sailex.secondbrain.mode.ModeController;
import me.sailex.secondbrain.model.Goal;
import me.sailex.secondbrain.model.context.BlockData;
import me.sailex.secondbrain.model.context.ItemData;
import me.sailex.secondbrain.thread.GoalThread;
import me.sailex.secondbrain.util.LogUtil;
import me.sailex.secondbrain.util.MCDataUtil;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;

import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

/**
 * Controller of a npc player.
 * Queues and executes actions (npc capabilities) added by
 * {@link OpenAiFunctionManager},
 * {@link OllamaFunctionManager}
 * or {@link ModeController}
 *
 * @author sailex
 */
public class NPCController implements Tickable {

	private final BlockingQueue<Goal> goalQueue;
	private GoalThread goalThread;

	private final ServerPlayerEntity npcEntity;
	private final IBaritone automatone;
	private final ContextProvider contextProvider;

	private boolean isIdling = true;

	public NPCController(
		ServerPlayerEntity npcEntity,
		IBaritone automatone,
		ContextProvider contextProvider
	) {
		this.npcEntity = npcEntity;
		this.automatone = automatone;
		this.contextProvider = contextProvider;
		this.goalQueue = new LinkedBlockingQueue<>();
		this.goalThread = new GoalThread();
		registerTickListener();
	}

	/**
	 * Queues a goal or executes it directly if {@code executeDirectly} is true.
	 *
	 * @param goalTask 	 	   the action to execute
	 * @param goalName		   the name of the action
	 * @param executeDirectly  whether action should be executed directly ()
	 */
	public void addGoal(
		String goalName,
		Runnable goalTask,
		boolean executeDirectly
	) {
		if (executeDirectly) {
			goalTask.run();
			return;
		}
		goalQueue.add(new Goal(goalName, goalTask));
	}

	public void addGoal(String goalName, Runnable goalTask) {
		addGoal(goalName, goalTask, false);
	}

	/**
	 * Polls and executes the next action in the queue.
	 * Sets {@link #isIdling} to true if no actions are left in the queue.
	 */
	private void pollGoal() {
		if (goalThread.isCompleted()) {
			Goal nextGoal = goalQueue.poll();

			if (nextGoal != null) {
				this.isIdling = false;
				goalThread = new GoalThread(nextGoal);
				goalThread.start();
			} else {
				isIdling = true;
			}
		}
	}

	/**
	 * Sends a chat message to all players.
	 * @param message the message to send
	 */
	public void chat(String message) {
		npcEntity.server.getPlayerManager().broadcast(SignedMessage.ofUnsigned(message), npcEntity, MessageType.params(MessageType.CHAT, npcEntity));
		npcEntity.sendMessage(Text.of(message), false);
	}

	/**
	 * Moves to an entity/player.
	 * @param entityName the player name to move to
	 */
	public void moveToEntity(String entityName, boolean isPlayer) {
		Entity entity;
		if (isPlayer) {
			entity = MCDataUtil.getNearbyPlayer(entityName, npcEntity);
		} else {
			entity = MCDataUtil.getNearbyEntity(entityName, npcEntity);
		}

		if (entity != null) {
			this.moveToCoordinates(entity.getBlockPos());
		} else {
			LogUtil.error("Unable to move to entity: " + entityName + " - entity not found!", true);
		}
	}

	/**
	 * Moves to specific coordinates.
	 * @param targetPosition the target position to move to
	 */
	public void moveToCoordinates(BlockPos targetPosition) {
		automatone.getCustomGoalProcess()
				.setGoalAndPath(new GoalBlock(targetPosition.getX(), targetPosition.getY(), targetPosition.getZ()));
	}

	/**
	 * Moves away (20 Blocks) in a random direction.
	 */
	public void moveAway() {
		automatone.getCustomGoalProcess().setGoalAndPath(new GoalRunAway(20, npcEntity.getBlockPos()));
	}

	/**
	 * Mines a block of a specific type.
	 *
	 * @param blockType		  block type to mine
	 * @param numberOfBlocks  number of blocks to mine
	 */
	public void mineBlock(String blockType, int numberOfBlocks) {
		List<BlockData> blocks = contextProvider.getChunkManager().getBlocksOfType(blockType, numberOfBlocks);
		if (!blocks.isEmpty()) {
			for (int i = 0; i < numberOfBlocks; i++) {
				BlockData block = blocks.get(i);
				mine(block.position());
			}
		} else {
			LogUtil.debugInChat("Couldnt find blocks of type: " + blockType + " to mine!");
		}
	}

	private void mine(BlockPos targetPosition) {
		automatone.getBuilderProcess().clearArea(targetPosition, targetPosition);
	}

	/**
	 * Attacks a nearby entity/player.
	 *
	 * @param entityName player name or entity type
	 * @param isPlayer 	 whether entity is a player
	 */
	public void attackEntity(String entityName, boolean isPlayer) {
		Entity entity;
		if (isPlayer) {
			entity = MCDataUtil.getNearbyPlayer(entityName, npcEntity);
		} else {
			entity = MCDataUtil.getNearbyEntity(entityName, npcEntity);
		}

		if (entity != null) {
			this.attack(entity);
		} else {
			LogUtil.error("Couldnt find " + entityName + " to attack!", true);
		}
	}

	private void attack(Entity targetEntity) {
		moveToCoordinates(targetEntity.getBlockPos());
		npcEntity.lookAt(EntityAnchorArgumentType.EntityAnchor.EYES, targetEntity.getEyePos());
		automatone.getCommandHelper().executeAttack();
		npcEntity.swingHand(npcEntity.getActiveHand());
	}

	/**
	 * Drops an item from the inventory.
	 *
	 * @param itemType item type to drop
	 * @param dropAll  whether to drop all items of the same type
	 */
	public void dropItem(String itemType, boolean dropAll) {
		Optional<ItemData> item = contextProvider.getCachedContext().findItemByType(itemType);
		if (item.isPresent()) {
			int slot = item.get().slot();
			if (dropAll) {
				automatone.getCommandHelper().executeDropAll(slot);
			} else {
				automatone.getCommandHelper().executeDrop(slot);
			}
		} else {
			LogUtil.debugInChat("Couldnt find item of type: " + itemType + " to drop!");
		}
	}

	//will be impl if all other things are tested with this function calling
	public void craftItem(String itemName) {
		Identifier identifier = Identifier.of(itemName);

		RecipeEntry<?> recipe = npcEntity.getServer().getRecipeManager().get(identifier).orElse(null);
//		ClientPlayerInteractionManager interactionManager = client.interactionManager;
//		if (recipe != null && interactionManager != null) {
//			interactionManager.clickRecipe(npc.currentScreenHandler.syncId, recipe, false);
//		} else {
//			LOGGER.warn("Could not find recipe with id: {}", recipeId);
//		}
	}

	public void jump() {
		automatone.getCommandHelper().executeJump();
	}

	private void lookAtPlayer() {
		if (!goalQueue.isEmpty()) return;
		PlayerEntity closestPlayer = MCDataUtil.getClosestPlayer(npcEntity);
		if (closestPlayer != null) {
			npcEntity.lookAt(EntityAnchorArgumentType.EntityAnchor.EYES, closestPlayer.getEyePos());
		}
	}

	private void autoRespawn() {
		if (npcEntity.isDead()) {
			npcEntity.server.getPlayerManager().remove(npcEntity);
			automatone.getCommandHelper().executeSpawn(npcEntity.getName().getString());
		}
	}

	/**
	 * Processes npc actions on game tick
	 */
	@Override
	public void onTick() {
		autoRespawn();
		if (!isActive()) {
			lookAtPlayer();
			pollGoal();
		}
	}

	private boolean isActive() {
		return automatone.getPathingBehavior().isPathing()
				|| automatone.getCustomGoalProcess().isActive()
				|| automatone.getMineProcess().isActive();
	}

	public void cancelActions() {
		goalQueue.clear();
		if (!goalThread.isCompleted()) {
			goalThread.interrupt();
		}
		cancelAutomatone();
	}

	private void cancelAutomatone() {
        try {
            automatone.getCommandManager().execute(npcEntity.getCommandSource(), "cancel");
        } catch (CommandException e) {
			LogUtil.error("Error executing automatone cancel command" + e, true);
        }
    }

	public boolean isIdling() {
		return isIdling;
	}
}
