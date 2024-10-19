package io.sailex.aiNpc.command;

import static net.minecraft.server.command.CommandManager.literal;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.sailex.aiNpc.model.messageTypes.InstructionMessage;
import io.sailex.aiNpc.npc.NPCController;
import io.sailex.aiNpc.npc.NPCManager;
import io.sailex.aiNpc.util.FeedbackLogger;
import java.util.Map;
import java.util.Optional;
import lombok.AllArgsConstructor;
import net.minecraft.command.argument.MessageArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

@AllArgsConstructor
public class NPCDoCommand {

	private final NPCManager npcManager;

	public LiteralArgumentBuilder<ServerCommandSource> getCommand() {
		return literal("do")
				.then(CommandManager.argument("name", StringArgumentType.string())
						.then(CommandManager.argument("message", MessageArgumentType.message())
								.executes(this::doNPC)));
	}

	private int doNPC(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		String name = StringArgumentType.getString(context, "name");

		Optional<NPCController> npcController = npcManager.getNpcControllers().entrySet().stream()
				.filter(entry -> entry.getKey().getNpcName().equals(name))
				.map(Map.Entry::getValue)
				.findFirst();

		if (npcController.isEmpty()) {
			context.getSource()
					.sendFeedback(
							FeedbackLogger.logError(String.format("NPC with name %s does not exist.", name)), true);
			return 0;
		}

		String message = MessageArgumentType.getMessage(context, "message").getString();
		npcController.get().handleInstruction(new InstructionMessage(message));
		return 1;
	}
}
