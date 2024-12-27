package io.sailex.ai.npc.client.listeners

import io.sailex.ai.npc.client.model.NPC
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents
import net.minecraft.entity.player.PlayerEntity

class EntityLoadListener(
    npc: NPC,
) : AEventListener(npc) {

    override fun register() {
        ClientEntityEvents.ENTITY_LOAD.register { entity, _ ->
            if (npc.id == entity.getUuid() || entity !is PlayerEntity) {
                return@register
            }
            val entityLoadMessage =
                String.format(
                    "A Player with the name: %s loaded in world at x: %s y: %s z: %s",
                    entity.name.string,
                    entity.pos.x,
                    entity.pos.y,
                    entity.pos.z,
                )
            handleMessage(entityLoadMessage)
        }
    }
}
