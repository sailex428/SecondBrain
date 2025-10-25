package me.sailex.secondbrain.mixin;

import net.minecraft.network.PacketByteBuf;
import org.spongepowered.asm.mixin.Mixin;

//? if <=1.20.1 {

import io.wispforest.endec.Endec;
import io.wispforest.endec.SerializationContext;
import io.wispforest.endec.format.bytebuf.ByteBufDeserializer;
import io.wispforest.endec.format.bytebuf.ByteBufSerializer;
import io.wispforest.endec.util.EndecBuffer;

@Mixin(PacketByteBuf.class)
public abstract class PacketByteBufMixin implements EndecBuffer {

    @Override
    public <T> void write(SerializationContext ctx, Endec<T> endec, T value) {
        endec.encodeFully(ctx, () -> ByteBufSerializer.of((PacketByteBuf) (Object) this), value);
    }

    @Override
    public <T> T read(SerializationContext ctx, Endec<T> endec) {
        return endec.decodeFully(ctx, ByteBufDeserializer::of, (PacketByteBuf) (Object) this);
    }
}
//?} else {
/*@Mixin(PacketByteBuf.class)
public abstract class PacketByteBufMixin {

}
*///?}
