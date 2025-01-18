package me.sailex.ai.npc.model.interaction;

/**
 * Represents the type of action that can be taken by the NPC
 */
public enum ActionType {
	CHAT, // Talking to other entities
	MOVE, // Moving to a location
	MINE, // Gathering resources
	CRAFT, // Creating items
	DROP, // Dropping items
	ATTACK, // Combat actions
	// USE, // Using items
	// STORE, // Putting items away
	// RETRIEVE, // Getting items e.g. kill entities
	// EQUIP, // Equipping items
	// UNEQUIP, // Removing equipped items
	// TRADE, // Trading with Villagers
	// BUILD, // Construction actions
	// REPAIR, // Fixing items
	// INTERACT, // General interactions
	STOP // Cancelling actions
}
