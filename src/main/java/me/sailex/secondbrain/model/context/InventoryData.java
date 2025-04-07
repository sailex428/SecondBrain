package me.sailex.secondbrain.model.context;

import java.util.ArrayList;
import java.util.List;

public record InventoryData(
    List<ItemData> armor,
    List<ItemData> mainInventory,
    List<ItemData> hotbar,
    List<ItemData> offHand
) {
    public List<ItemData> getAllItems() {
        List<ItemData> allItems = new ArrayList<>();
        allItems.addAll(armor);
        allItems.addAll(mainInventory);
        allItems.addAll(hotbar);
        allItems.addAll(offHand);
        return allItems;
    }
}
