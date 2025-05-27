package me.sailex.secondbrain.listener

import me.sailex.secondbrain.model.NPC
import net.minecraft.entity.player.PlayerEntity
import java.util.UUID

abstract class AEventListener(
    protected val npcs: Map<UUID, NPC>
) : IEventListener {

    abstract override fun register()

    protected fun getMatchingNpc(player: PlayerEntity): NPC? {
        return npcs.values.firstOrNull { it.entity.uuid == player.uuid }
    }

}
