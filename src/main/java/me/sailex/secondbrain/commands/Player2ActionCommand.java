package me.sailex.secondbrain.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import lombok.AllArgsConstructor;
import me.sailex.secondbrain.common.Player2NpcSynchronizer;
import me.sailex.secondbrain.util.LogUtil;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Arrays;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

@AllArgsConstructor
public class Player2ActionCommand {

    private final Player2NpcSynchronizer synchronizer;

    private enum PLAYER2_ACTION {
        SYNC
    }

    public LiteralArgumentBuilder<ServerCommandSource> getCommand() {
        return literal("player2")
                    .then(argument("action", StringArgumentType.string())
                            .suggests((context, builder) -> {
                                for (PLAYER2_ACTION action : PLAYER2_ACTION.values()) {
                                    builder.suggest(action.toString());
                                }
                                return builder.buildFuture();
                            }).executes(this::executeSyncCharacters));
    }

    private int executeSyncCharacters(CommandContext<ServerCommandSource> context) {
        PLAYER2_ACTION action = PLAYER2_ACTION.valueOf(StringArgumentType.getString(context, "action"));

        if (action == PLAYER2_ACTION.SYNC) {
            ServerPlayerEntity player = context.getSource().getPlayer();
            if (player != null) {
                synchronizer.syncCharacters(player.getBlockPos());
            } else {
                synchronizer.syncCharacters();
            }
            return 1;
        }
        context.getSource().sendFeedback(() -> LogUtil.formatError("Action '" + action + "' does not exist. " +
                        "Available actions: " + Arrays.toString(PLAYER2_ACTION.values())),
                false);
        return 0;
    }

}
