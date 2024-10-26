package io.sailex.aiNpc.util;

import net.minecraft.text.Text;

public class ChatUtils {

	public static Text format(String message, String npcName) {
		return Text.of(String.format("[%s] %s", npcName, message));
	}
}
