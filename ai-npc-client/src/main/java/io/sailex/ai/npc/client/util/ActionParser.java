package io.sailex.ai.npc.client.util;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import io.sailex.ai.npc.client.model.interaction.Action;
import io.sailex.ai.npc.client.model.interaction.Actions;

public class ActionParser {

	private static final Gson GSON = new Gson();

	public static String actionToJson(Action action) {
		return GSON.toJson(action);
	}

	public static Action parseSingleAction(String action) throws JsonSyntaxException {
		return parse(action, Action.class);
	}

	public static Actions parseActions(String actions) throws JsonSyntaxException {
		return parse(actions, Actions.class);
	}

	private static <T> T parse(String action, Class<T> type) throws JsonSyntaxException {
		return GSON.fromJson(action, type);
	}
}
