package me.sailex.secondbrain.mixin;

import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LivingEntity.class)
public interface LivingEntityAccessor {

    @Accessor("lastDamageTime")
    long getLastDamageTime();

    @Accessor("lastDamageTaken")
    float getLastDamageTaken();

}
