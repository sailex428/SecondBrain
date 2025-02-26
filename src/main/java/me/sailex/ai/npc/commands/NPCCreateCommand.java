package me.sailex.ai.npc.commands;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import carpet.patches.EntityPlayerMPFake;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import lombok.AllArgsConstructor;
import me.sailex.ai.npc.exception.LLMServiceException;
import me.sailex.ai.npc.exception.NPCCreationException;
import me.sailex.ai.npc.llm.LLMType;
import me.sailex.ai.npc.NPCFactory;
import me.sailex.ai.npc.util.LogUtil;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import net.minecraft.world.GameMode;
import net.minecraft.world.World;

import java.util.Objects;
import java.util.concurrent.CountDownLatch;

@AllArgsConstructor
public class NPCCreateCommand {

	private static final String LLM_TYPE = "llm-type";
	private static final String LLM_MODEL = "llm-model";

	private final NPCFactory npcFactory;

	public LiteralArgumentBuilder<ServerCommandSource> getCommand() {
		return literal("add")
				.requires(source -> source.hasPermissionLevel(2))
				.then(argument("name", StringArgumentType.string())
						.then(argument(LLM_TYPE, StringArgumentType.string())
								.suggests((context, builder) -> {
									for (LLMType llmType : LLMType.getEntries()) {
										builder.suggest(llmType.toString());
									}
									return builder.buildFuture();
								}).executes(this::createNpcWithLLM)));
	}

	private int createNpcWithLLM(CommandContext<ServerCommandSource> context) {
		if (context.getSource().getPlayer() == null) {
			context.getSource().sendFeedback(() -> LogUtil.formatError("Command must be executed as a Player!"), false);
			return 0;
		}
		String name = StringArgumentType.getString(context, "name");
		String llmType = StringArgumentType.getString(context, LLM_TYPE);

		try {
			spawnNpc(context.getSource(), name);
			CountDownLatch latch = new CountDownLatch(1);
			checkPlayerAvailable(name, latch);

			new Thread(() -> {
                try {
                    latch.await();
                	ServerPlayerEntity npc = context.getSource().getServer().getPlayerManager().getPlayer(name);
					npcFactory.createNpc(context.getSource().getServer(), Objects.requireNonNull(npc), llmType);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (NPCCreationException e) {
					LogUtil.error(e.getMessage());
				}
			}).start();

			LogUtil.info(("Created NPC with name: " + name + ", LLM Type: " + llmType));
			return 1;
		} catch (LLMServiceException | NullPointerException e) {
			context.getSource().sendFeedback(() -> LogUtil.formatError(e.getMessage()), false);
			return 0;
		}
	}

	private void spawnNpc(ServerCommandSource source, String name) {
		RegistryKey<World> dimensionKey = source.getWorld().getRegistryKey();
		ServerPlayerEntity player = source.getPlayer();

		boolean isSuccessful = EntityPlayerMPFake.createFake(name, source.getServer(),
				player.getPos(), player.getYaw(), player.getPitch(),
				dimensionKey, GameMode.SURVIVAL, false);
		if (!isSuccessful) {
			throw new NullPointerException("Player profile doesn't exist!");
		}
	}

	private void checkPlayerAvailable(String npcName, CountDownLatch latch) {
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			if (server.getPlayerManager().getPlayer(npcName) != null) {
				latch.countDown();
			}
		});
	}

}
