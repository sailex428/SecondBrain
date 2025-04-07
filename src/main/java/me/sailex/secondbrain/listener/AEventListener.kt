package me.sailex.secondbrain.listener

import me.sailex.secondbrain.model.NPC
import net.minecraft.entity.player.PlayerEntity

abstract class AEventListener(
    protected val npcs: Map<String, NPC>
) : IEventListener {

    abstract override fun register()

    protected fun getMatchingNpc(player: PlayerEntity): NPC? {
        return npcs[player.name.string]
    }

}
