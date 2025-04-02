package me.sailex.secondbrain.networking.packet;

import io.wispforest.endec.Endec;
import io.wispforest.endec.StructEndec;
import io.wispforest.endec.impl.StructEndecBuilder;
import me.sailex.secondbrain.config.NPCConfig;

public record AddNpcPacket(NPCConfig npcConfig, boolean isNewConfig) {

    public static final StructEndec<AddNpcPacket> ENDEC = StructEndecBuilder.of(
            NPCConfig.ENDEC.fieldOf("npcConfig", AddNpcPacket::npcConfig),
            Endec.BOOLEAN.fieldOf("isNewConfig", AddNpcPacket::isNewConfig),
            AddNpcPacket::new
    );

    @Override
    public String toString() {
        return "AddNpcPacket{" + npcConfig + "}";
    }
}
