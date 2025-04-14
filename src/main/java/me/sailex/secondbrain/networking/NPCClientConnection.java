package me.sailex.secondbrain.networking;

import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.NetworkState;
import net.minecraft.network.listener.PacketListener;

public class NPCClientConnection extends ClientConnection {

    public NPCClientConnection(NetworkSide p) {
        super(p);
        ((ClientConnectionInterface) this).setChannel(new EmbeddedChannel());
    }

    @Override
    public void tryDisableAutoRead() {}

    @Override
    public void handleDisconnection() {}

    @Override
    public void setInitialPacketListener(PacketListener packetListener) {}

    @Override
    public <T extends PacketListener> void transitionInbound(NetworkState<T> protocolInfo, T packetListener) {}
}
