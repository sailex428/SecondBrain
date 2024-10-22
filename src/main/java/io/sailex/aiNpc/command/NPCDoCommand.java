package io.sailex.aiNpc.command;

import static net.minecraft.server.command.CommandManager.literal;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.sailex.aiNpc.constant.ResponseSchema;
import io.sailex.aiNpc.model.NPC;
import io.sailex.aiNpc.model.event.InstructionMessageEvent;
import io.sailex.aiNpc.npc.NPCController;
import io.sailex.aiNpc.util.FeedbackLogger;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import net.minecraft.command.argument.MessageArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

@AllArgsConstructor
public class NPCDoCommand {

	private final List<NPC> npcList;

	public LiteralArgumentBuilder<ServerCommandSource> getCommand() {
		return literal("do")
				.then(CommandManager.argument("name", StringArgumentType.string())
						.then(CommandManager.argument("message", MessageArgumentType.message())
								.executes(this::doNPC)));
	}

	private int doNPC(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		String name = StringArgumentType.getString(context, "name");

		Optional<NPCController> npcController = npcList.stream()
				.filter(npc -> npc.getNpcEntity().getNpcName().equals(name))
				.map(NPC::getNpcController)
				.findFirst();

		if (npcController.isEmpty()) {
			context.getSource()
					.sendFeedback(
							FeedbackLogger.logError(String.format("NPC with name %s does not exist.", name)), true);
			return 0;
		}

		String message = MessageArgumentType.getMessage(context, "message").getString();
		npcController.get().handleMessage(new InstructionMessageEvent(message), ResponseSchema.CHAT_MESSAGE);
		return 1;
	}
}
