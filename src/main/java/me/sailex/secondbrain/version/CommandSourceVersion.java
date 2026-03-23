package me.sailex.secondbrain.version;

//? >=1.21.11 {
/*import net.minecraft.command.permission.Permission;
import net.minecraft.command.permission.PermissionLevel;
*///?}
import net.minecraft.server.command.ServerCommandSource;


public class CommandSourceVersion {

    private CommandSourceVersion() {}

    public static boolean hasPermissionLevel(ServerCommandSource source, int level) {
        //? >=1.21.11 {
        /*return source.getPermissions().hasPermission(new Permission.Level(PermissionLevel.fromLevel(level)));
        *///?} else {
        return source.hasPermissionLevel(level);
        //?}
    }

}
