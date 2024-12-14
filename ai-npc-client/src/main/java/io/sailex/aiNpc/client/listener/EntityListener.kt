package io.sailex.aiNpc.client.listener

import io.sailex.aiNpc.client.model.NPC
import io.sailex.aiNpc.client.model.interaction.ActionType
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents
import net.minecraft.entity.player.PlayerEntity

class EntityListener(npc: NPC) : AEventListener(npc) {

    override fun register() {
        ServerEntityEvents.ENTITY_LOAD.register { entity, world ->
            if (npc.id == entity.getUuid() || entity !is PlayerEntity) {
                return@register
            }
            val entityLoadMessage = String.format("A Player with the name: %S loaded in world at x: %S y: %S z: %S",
                entity.name.string, entity.pos.x, entity.pos.y, entity.pos.z)

            logger.info(entityLoadMessage)
            handleMessage(ActionType.INTERACT, entityLoadMessage)
        }
    }

}