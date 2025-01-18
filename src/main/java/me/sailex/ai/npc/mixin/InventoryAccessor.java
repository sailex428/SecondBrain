package me.sailex.ai.npc.mixin;

import java.util.List;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PlayerInventory.class)
public interface InventoryAccessor {

	@Accessor
	List<DefaultedList<ItemStack>> getCombinedInventory();
}
