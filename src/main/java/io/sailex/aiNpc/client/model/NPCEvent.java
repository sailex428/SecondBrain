package io.sailex.aiNpc.client.model;

import io.sailex.aiNpc.client.model.interaction.ActionType;

public record NPCEvent(ActionType type, String message) {}
