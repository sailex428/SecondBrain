package me.sailex.secondbrain.listener

import me.sailex.secondbrain.model.NPC
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents
import net.minecraft.entity.player.PlayerEntity

class EntityLoadListener(
    npcs: Map<String, NPC>
) : AEventListener(npcs) {

    override fun register() {
        ServerEntityEvents.ENTITY_LOAD.register { entity, _ ->
            if (entity !is PlayerEntity) {
                return@register
            }
            getMatchingNpc(entity)?.eventHandler?.onEvent(entity.name.string + " joined the server!")
        }
    }
}
