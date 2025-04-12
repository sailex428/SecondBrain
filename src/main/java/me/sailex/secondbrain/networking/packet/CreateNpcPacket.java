package me.sailex.secondbrain.networking.packet;

import io.wispforest.endec.StructEndec;
import io.wispforest.endec.impl.StructEndecBuilder;
import me.sailex.secondbrain.config.NPCConfig;

public record CreateNpcPacket(NPCConfig npcConfig) {

    public static final StructEndec<CreateNpcPacket> ENDEC = StructEndecBuilder.of(
            NPCConfig.ENDEC.fieldOf("npcConfig", CreateNpcPacket::npcConfig),
            CreateNpcPacket::new
    );

    @Override
    public String toString() {
        return "AddNpcPacket{npcConfig=" + npcConfig + "}";
    }
}
