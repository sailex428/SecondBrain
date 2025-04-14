package me.sailex.secondbrain.mixin;

import carpet.logging.logHelpers.PacketCounter;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import me.sailex.secondbrain.networking.ClientConnectionInterface;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.packet.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientConnection.class)
public abstract class ClientConnectionMixin implements ClientConnectionInterface {

    @Inject(method = "channelRead0", at = @At("HEAD"))
    private void packetInCount(ChannelHandlerContext channelHandlerContext, Packet<?> packet, CallbackInfo ci) {
        PacketCounter.totalIn++;
    }

    @Inject(method = "send", at = @At("HEAD"))
    private void packetOutCount(final Packet<?> packet, final PacketCallbacks packetSendListener, final boolean bl, final CallbackInfo ci) {
        PacketCounter.totalOut++;
    }

    @Override
    @Accessor //Compat with adventure-platform-fabric
    public abstract void setChannel(Channel channel);
}