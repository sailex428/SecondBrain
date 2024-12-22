package io.sailex.ai.npc.client.model.interaction;

import java.util.List;
import lombok.Getter;

/**
 * Wrapper for action class
 * Represents a list of actions that can be taken by the NPC
 * @see Action
 * @see ActionType
 */
@Getter
public class Actions {

	private List<Action> actions;
}
