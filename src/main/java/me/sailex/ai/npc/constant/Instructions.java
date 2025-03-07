package me.sailex.ai.npc.constant;

/**
 * Instructions for the LLM
 */
public class Instructions {

	private Instructions() {}

	public static String getInitPrompt(String npcName) {
		return String.format("Your ingame name is %s. Start with a chat message to say hello to the other players and move to around to explore the world!", npcName);
	}

	public static String SYSTEM_PROMPT = "You are an AI-controlled Minecraft NPC.";
}
