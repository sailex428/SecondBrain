package me.sailex.secondbrain.networking.packet;

import io.wispforest.endec.StructEndec;
import io.wispforest.endec.impl.StructEndecBuilder;
import me.sailex.secondbrain.config.NPCConfig;

public record UpdateNpcConfigPacket(NPCConfig npcConfig) {

    public static final StructEndec<UpdateNpcConfigPacket> ENDEC = StructEndecBuilder.of(
            NPCConfig.ENDEC.fieldOf("npcConfig", UpdateNpcConfigPacket::npcConfig),
            UpdateNpcConfigPacket::new
    );

    @Override
    public String toString() {
        return "UpdateNpcConfigPacket={npcConfig=" + npcConfig + "}";
    }

}
