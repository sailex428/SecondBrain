package io.sailex.ai.npc.client.model;

import io.sailex.ai.npc.client.model.interaction.ActionType;

/**
 * Represents an event that is triggered in the game world
 */
public record NPCEvent(ActionType type, String message) {}
