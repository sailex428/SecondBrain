package me.sailex.secondbrain.networking.packet;

import io.wispforest.endec.StructEndec;
import io.wispforest.endec.impl.StructEndecBuilder;
import me.sailex.secondbrain.config.NPCConfig;

public record AddNpcPacket(NPCConfig npcConfig) {

    public static final StructEndec<AddNpcPacket> ENDEC = StructEndecBuilder.of(
            NPCConfig.ENDEC.fieldOf("npcConfig", AddNpcPacket::npcConfig),
            AddNpcPacket::new
    );

    @Override
    public String toString() {
        return "AddNpcPacket{" + npcConfig + "}";
    }
}
