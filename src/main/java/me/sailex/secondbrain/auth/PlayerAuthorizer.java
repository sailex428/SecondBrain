package me.sailex.secondbrain.auth;

import io.wispforest.owo.network.ServerAccess;

public class PlayerAuthorizer {

    /**
     * The player is considered authorized if they have the "operator" permission level.
     *
     * @param serverAccess the context providing server-related access, including the player's details
     * @return true if the player is authorized, otherwise false
     */
    public boolean isAuthorized(ServerAccess serverAccess) {
        return serverAccess.player().hasPermissionLevel(2);
    }

    public boolean isLocalConnection(ServerAccess serverAccess) {
        String address = serverAccess.player().networkHandler.getConnectionAddress().toString();
        return serverAccess.runtime().isSingleplayer() || address.equals("127.0.0.1");
    }

}
