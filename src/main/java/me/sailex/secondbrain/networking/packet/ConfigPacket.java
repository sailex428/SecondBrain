package me.sailex.secondbrain.networking.packet;

import io.wispforest.endec.StructEndec;
import io.wispforest.endec.impl.StructEndecBuilder;
import me.sailex.secondbrain.config.BaseConfig;
import me.sailex.secondbrain.config.NPCConfig;

import java.util.List;

public record ConfigPacket(BaseConfig baseConfig, List<NPCConfig> npcConfigs) {

    public static final StructEndec<ConfigPacket> ENDEC = StructEndecBuilder.of(
            BaseConfig.ENDEC.fieldOf("baseConfig", ConfigPacket::baseConfig),
            NPCConfig.ENDEC.listOf().fieldOf("npcConfigs", ConfigPacket::npcConfigs),
            ConfigPacket::new
    );

    /**
     * Removes llm secret from the npcConfigs.
     */
    public void hideSecret() {
        npcConfigs.forEach(config -> config.setOpenaiApiKey("this wont be ever shared with any client. (to overwrite it create a new NPC)"));
    }

    @Override
    public String toString() {
        return "ConfigPacket{" +
                "baseConfig=" + baseConfig +
                ",npcConfigs=" + npcConfigs +
                '}';
    }

}
