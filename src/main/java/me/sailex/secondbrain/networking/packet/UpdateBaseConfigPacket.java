package me.sailex.secondbrain.networking.packet;

import io.wispforest.endec.StructEndec;
import io.wispforest.endec.impl.StructEndecBuilder;
import me.sailex.secondbrain.config.BaseConfig;

public record UpdateBaseConfigPacket(BaseConfig baseConfig) {

    public static final StructEndec<UpdateBaseConfigPacket> ENDEC = StructEndecBuilder.of(
            BaseConfig.ENDEC.fieldOf("npcConfig", UpdateBaseConfigPacket::baseConfig),
            UpdateBaseConfigPacket::new
    );

    @Override
    public String toString() {
        return "UpdateBaseConfigPacket={baseConfig=" + baseConfig + "}";
    }

}
