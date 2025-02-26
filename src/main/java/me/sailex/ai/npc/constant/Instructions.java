package me.sailex.ai.npc.constant;

import lombok.Getter;

/**
 * Instructions for the LLM
 */
public class Instructions {

	private Instructions() {}

	public static String getDefaultInstruction(String npcName) {
		return String.format(
				"""
				Your ingame name is %s.
				Start with a chat message to say hello to the other players.
				""",
				npcName);
	}

	public static final String PROMPT_PREFIX = "Please use the tool functions provided!! ";

	@Getter
	private static final String FORMATTING_INSTRUCTION =
			"""
				When responding to a request, use action types CHAT, MOVE, MINE, DROP, STOP, CRAFT, depending on the context.
				Each action should be clearly defined with an appropriate message and the needed fields.
				Always structure responses like the examples in the "relevant data" section.
				You could also use the copy the structure and just fill it with your data.
				You should combine multiple actions (e.g., CHAT, MOVE, DROP) to fulfill the request.
				e.g. for DROP you must specify just the block name in targetType. For CRAFT you need the whole id in targetType.
				You can also do skills without CHAT messages.
				""";
}
