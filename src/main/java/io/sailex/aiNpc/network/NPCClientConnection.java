package io.sailex.aiNpc.network;

import io.netty.channel.embedded.EmbeddedChannel;
import io.sailex.aiNpc.mixin.ClientConnectionAccessor;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkSide;

public class NPCClientConnection extends ClientConnection {

	public NPCClientConnection(NetworkSide side) {
		super(side);
		((ClientConnectionAccessor) this).setChannel(new EmbeddedChannel());
	}

	@Override
	public void handleDisconnection() {}
}
