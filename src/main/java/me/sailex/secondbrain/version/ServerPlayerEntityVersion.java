package me.sailex.secondbrain.version;

//? >=1.21.11 {
/*import net.minecraft.command.permission.Permission;
import net.minecraft.command.permission.PermissionLevel;
*///?}
import net.minecraft.server.network.ServerPlayerEntity;

public final class ServerPlayerEntityVersion {

    private ServerPlayerEntityVersion() {}

    public static boolean hasPermissionLevel(ServerPlayerEntity player, int level) {
        //? >=1.21.11 {
        /*return player.getPermissions().hasPermission(new Permission.Level(PermissionLevel.fromLevel(level)));
        *///?} else {
        return player.hasPermissionLevel(level);
         //?}
    }

}
