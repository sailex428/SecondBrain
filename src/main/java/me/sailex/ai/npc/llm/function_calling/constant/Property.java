package me.sailex.ai.npc.llm.function_calling.constant;

public class Property {

    private Property() {}

    public static class Name {
        private Name() {}

        public static final String MESSAGE = "message";
        public static final String X = "x";
        public static final String Y = "y";
        public static final String Z = "z";
        public static final String SLOT = "slot";
        public static final String ENTITY_ID = "entity_id";
        public static final String TOPIC = "topic";
        public static final String ITEM_NAME = "item_name";
    }

    public static class Description {
        private Description() {}

        public static final String MESSAGE = "Represents the message that will be printed in the game chat.";
        public static final String SLOT = "slot number of the item in the inventory";
        public static final String ENTITY_ID = "entity id of the Entity that is to be attacked";
        public static final String ITEM_NAME = "item for which a recipe is wanted";
        public static final String TOPIC = "Topic for which old conversations should be searched for";
    }

}
