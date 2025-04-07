package me.sailex.secondbrain.networking.packet;

import io.wispforest.endec.Endec;
import io.wispforest.endec.StructEndec;
import io.wispforest.endec.impl.StructEndecBuilder;

public record DeleteNpcPacket(String npcName, boolean isDeleteConfig) {

    public static final StructEndec<DeleteNpcPacket> ENDEC = StructEndecBuilder.of(
            Endec.STRING.fieldOf("npcName", DeleteNpcPacket::npcName),
            Endec.BOOLEAN.fieldOf("isDeleteConfig", DeleteNpcPacket::isDeleteConfig),
            DeleteNpcPacket::new
    );

    @Override
    public String toString() {
        return "DeleteNpcPacket{npcUuid=" + npcName + ",isDeleteConfig=" + isDeleteConfig + "}";
    }
}