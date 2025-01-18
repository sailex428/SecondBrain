package me.sailex.ai.npc.model.interaction;

import java.util.List;
import lombok.Getter;

/**
 * Wrapper for action class
 * Represents a list of actions that can be taken by the NPC
 * @see Action
 * @see ActionType
 */
@Getter
public class Skill {

	private String skillName;
	private List<Action> actions;
}
