package me.sailex.secondbrain.networking.packet;

import io.wispforest.endec.Endec;
import io.wispforest.endec.StructEndec;
import io.wispforest.endec.impl.StructEndecBuilder;

public record DeleteNpcPacket(String npcName, boolean isDelete) {

    public static final StructEndec<DeleteNpcPacket> ENDEC = StructEndecBuilder.of(
            Endec.STRING.fieldOf("npcName", DeleteNpcPacket::npcName),
            Endec.BOOLEAN.fieldOf("isDelete", DeleteNpcPacket::isDelete),
            DeleteNpcPacket::new
    );

    @Override
    public String toString() {
        return "DeleteNpcPacket{npcUuid=" + npcName + ",isDelete=" + isDelete + "}";
    }
}