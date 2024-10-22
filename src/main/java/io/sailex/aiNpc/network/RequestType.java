package io.sailex.aiNpc.network;

public enum RequestType {
	CHAT_MESSAGE("chat_message"),
	INSTRUCTION("instruction"),
	HEALTH("health"),
	ENTITY_LOAD("entity_load"),
	SLEEP("sleep"),
	COMBAT("combat"),
	BLOCK_INTERACTION("block_interaction");

	RequestType(String type) {}
}
