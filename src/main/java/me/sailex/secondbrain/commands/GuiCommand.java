package me.sailex.secondbrain.commands;

import com.mojang.brigadier.context.CommandContext;
import lombok.AllArgsConstructor;
import me.sailex.secondbrain.config.ConfigProvider;
import me.sailex.secondbrain.config.NPCConfig;
import me.sailex.secondbrain.networking.NetworkHandler;
import me.sailex.secondbrain.networking.packet.ConfigPacket;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.stream.Collectors;

@AllArgsConstructor
public class GuiCommand {

    private final ConfigProvider configProvider;
    private final NetworkHandler networkHandler;

    public int execute(CommandContext<ServerCommandSource> context) {
        ServerPlayerEntity targetClient = context.getSource().getPlayer();
        if (targetClient != null) {
            ConfigPacket packet = new ConfigPacket(configProvider.getBaseConfig(),
                    configProvider.getNpcConfigs().stream().map(NPCConfig::deepCopy).collect(Collectors.toList()));
            packet.hideSecret();
            networkHandler.sendPacket(packet, targetClient);
            return 1;
        }
        return 0;
    }

}
