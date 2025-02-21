package me.sailex.ai.npc.llm.function_calling.constant;

public class Function {

    private Function() {}

    public static class Name {
        private Name() {}

        public static final String CHAT = "chat";
        public static final String MOVE = "move";
        public static final String MINE  = "mine";
        public static final String DROP = "drop";
        public static final String DROP_ALL = "dropAll";
        public static final String ATTACK = "attack";
        public static final String GET_ENTITIES = "getEntities";
        public static final String GET_BLOCKS = "getBlocks";
        public static final String GET_NPC_STATE = "getNpcState";
        public static final String GET_RECIPES = "getRecipes";
        public static final String GET_CONVERSATIONS = "getConversations";
        public static final String GET_LATEST_CONVERSATIONS = "getLatestConversations";
        public static final String STOP = "stop";
    }

    public static class Description {
        private Description() {}

        public static final String CHAT = "Print answers on user request into game chat so the players can read it.";
        public static final String MOVE = "Move to the given location";
        public static final String MINE = "Mine the block at the given location";
        public static final String DROP = "Drop one item of the given inventory slot";
        public static final String DROP_ALL = "Drop all items of the given inventory slot";
        public static final String ATTACK = "Attack the entity of the given entity id";
        public static final String GET_ENTITIES = "Get all entities and ids and players next to the npc player (you)";
        public static final String GET_BLOCKS = "Get all blocks next to the player";
        public static final String GET_NPC_STATE = "Get npc state (foodlevel, health, ...) and inventory items to slots";
        public static final String GET_RECIPES = "Get all recipes that matches the specified item";
        public static final String GET_CONVERSATIONS = "Get a conversation to a specific topic from the past";
        public static final String GET_LATEST_CONVERSATIONS = "Get the last 7 conversations (user prompts and answer of you with the functions that are called)";
        public static final String STOP = "Stop all running npc actions (Should only be used if expressly requested)";

    }
}
