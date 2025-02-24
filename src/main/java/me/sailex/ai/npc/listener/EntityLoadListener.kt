package me.sailex.ai.npc.listener

import me.sailex.ai.npc.model.NPC
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents
import net.minecraft.entity.player.PlayerEntity

class EntityLoadListener(
    npc: NPC
) : AEventListener(npc) {

    override fun register() {
        ServerEntityEvents.ENTITY_LOAD.register { entity, _ ->
            if (npc.entity.uuid == entity.getUuid() || entity !is PlayerEntity) {
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
            handleMessage("system", entityLoadMessage)
        }
    }
}
