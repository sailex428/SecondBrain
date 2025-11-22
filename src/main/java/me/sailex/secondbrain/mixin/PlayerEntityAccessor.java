package me.sailex.secondbrain.mixin;

import net.minecraft.entity.data.TrackedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

//? if >=1.21.10 {
/*import net.minecraft.entity.PlayerLikeEntity;
 *///?} else {

import net.minecraft.entity.player.PlayerEntity;

//?}


//? if >=1.21.10 {
/*@Mixin(PlayerLikeEntity.class)
public interface PlayerEntityAccessor {

    @Accessor("PLAYER_MODE_CUSTOMIZATION_ID")
    static TrackedData<Byte> getPlayerModelParts() {
        throw new AssertionError();
    }
}
*///?} else {

@Mixin(PlayerEntity.class)
public interface PlayerEntityAccessor {

    @Accessor("PLAYER_MODEL_PARTS")
    static TrackedData<Byte> getPlayerModelParts() {
        throw new AssertionError();
    }

}

//?}