package me.sailex.secondbrain.llm.function_calling.constant;

public class Property {

    private Property() {}

    public static class Name {
        private Name() {}

        public static final String MESSAGE = "message";
        public static final String X = "x";
        public static final String Y = "y";
        public static final String Z = "z";
        public static final String IS_PLAYER = "is_player";
        public static final String ENTITY_NAME = "entity_name";
        public static final String TOPIC = "topic";
        public static final String ITEM_NAME = "item_name";
        public static final String DROP_ALL = "drop_all";
        public static final String BLOCK_TYPE = "block_type";
        public static final String PLAYER_NAME = "player_name";
        public static final String NUMBER_OF_BLOCKS = "number_of_blocks";
    }

    public static class Description {
        private Description() {}

        public static final String MESSAGE = "represents the message that will be printed in the game chat.";
        public static final String SLOT = "slot number of the item in the inventory";
        public static final String ENTITY_NAME = "name of the Entity/Player that will to be attacked";
        public static final String ITEM_NAME = "item for which a recipe is wanted";
        public static final String TOPIC = "topic for which old conversations should be searched for";
        public static final String DROP_ALL = "whether to drop all items of the type or not";
        public static final String PLAYER_NAME = "name of a player";
        public static final String BLOCK_TYPE = "type of block that should be mined";
        public static final String IS_PLAYER = "whether the entity is a player or not";
        public static final String NUMBER_OF_BLOCKS = "number the blocks the npc will mine";
        public static final String X_COORDINATE = "x coordinate of the location";
        public static final String Y_COORDINATE = "y coordinate of the location";
        public static final String Z_COORDINATE = "z coordinate of the location";
    }

}
