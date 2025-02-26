package me.sailex.ai.npc.listener

import me.sailex.ai.npc.model.NPC
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
            val matchingNpc = getMatchingNpc(entity)
            if (matchingNpc != null) {
                val entityLoadMessage =
                    String.format(
                        "A Player with the name: %s loaded in world at x: %s y: %s z: %s",
                        entity.name.string,
                        entity.pos.x,
                        entity.pos.y,
                        entity.pos.z,
                    )
                matchingNpc.eventHandler.onEvent("user", entityLoadMessage)
            }
        }
    }
}
