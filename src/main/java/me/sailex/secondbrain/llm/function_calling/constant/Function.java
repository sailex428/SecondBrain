package me.sailex.secondbrain.llm.function_calling.constant;

public class Function {

    private Function() {}

    public static class Name {
        private Name() {}

//        public static final String CHAT = "chat";
        public static final String MOVE_TO_COORDINATES = "moveToCoordinates";
        public static final String MOVE_TO_ENTITY = "moveToEntity";
        public static final String MOVE_AWAY = "moveAway";
        public static final String MINE_BLOCK  = "mineBlock";
        public static final String DROP_ITEM = "dropItem";
        public static final String ATTACK_ENTITY = "attackEntity";
        public static final String GET_ENTITIES = "getEntities";
        public static final String GET_BLOCKS = "getBlocks";
        public static final String GET_RECIPES = "getRecipes";
        public static final String GET_CONVERSATIONS = "getConversations";
        public static final String STOP = "stop";
    }

    public static class Description {
        private Description() {}

//        public static final String CHAT = "Print answers into game chat so the players can read it.";
        public static final String MOVE_TO_COORDINATES = "Move to coordinates";
        public static final String MOVE_TO_ENTITY = "Move to entity/player";
        public static final String MOVE_AWAY = "Move away from current position in a random direction";
        public static final String MINE_BLOCK = "Mine a block by name";
        public static final String DROP_ITEM = "Drop item/s by name";
        public static final String ATTACK_ENTITY = "Attack an entity/player by name";
        public static final String GET_ENTITIES = "Get entities and players next to you";
        public static final String GET_BLOCKS = "Get all blocks next to you";
        public static final String GET_RECIPES = "Get all recipes that matches the specified item";
        public static final String GET_CONVERSATIONS = "Get a conversation to a specific topic from the past";
        public static final String STOP = "Stop all running npc actions (Should only be used if expressly requested)";
    }
}
