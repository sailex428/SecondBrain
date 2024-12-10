package io.sailex.aiNpc.client.util;

import io.sailex.aiNpc.client.AiNPCClient;
import io.sailex.aiNpc.client.config.Config;
import io.sailex.aiNpc.client.constant.ConfigConstants;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;

/**
 * Utility class for connecting the client to the server.
 */
public class ConnectionUtil {

    /**
     * Connect the client to the server.
     */
    public static void connectToServer() {
        String serverName = Config.getProperty(ConfigConstants.NPC_SERVER_IP);
        String port = Config.getProperty(ConfigConstants.NPC_SERVER_PORT);

        ConnectScreen.connect(
                AiNPCClient.client.currentScreen,
                AiNPCClient.client,
                new ServerAddress(serverName, Integer.parseInt(port)),
                new ServerInfo("server", serverName, ServerInfo.ServerType.OTHER),
                false
                /*? if >=1.21.1 {*//*, null*//*?}*/
        );
    }

}
