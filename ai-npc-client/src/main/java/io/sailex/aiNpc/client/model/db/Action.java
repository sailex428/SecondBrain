package io.sailex.aiNpc.client.model.db;

public record Action(int id, String name, float[] name_embedding, String description, float[] description_embedding, String example) {}
