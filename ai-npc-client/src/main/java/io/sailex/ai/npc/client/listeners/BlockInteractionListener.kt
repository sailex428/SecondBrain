package io.sailex.ai.npc.client.listeners

import io.sailex.ai.npc.client.model.NPC
import net.fabricmc.fabric.api.event.client.player.ClientPlayerBlockBreakEvents
import net.fabricmc.fabric.api.event.player.UseBlockCallback
import net.minecraft.util.ActionResult

/**
 * Listens to block interactions of this npc
 */
class BlockInteractionListener(
    npc: NPC,
) : AEventListener(npc) {
    override fun register() {
        ClientPlayerBlockBreakEvents.AFTER.register { _, player, pos, state ->
            if (player.uuid != npc.id) {
                return@register
            }
            val blockBreakMessage =
                String.format(
                    "You broke the block %s at %s",
                    state.block.name.string,
                    pos.toShortString(),
                )
            handleMessage(blockBreakMessage)
        }

        UseBlockCallback.EVENT.register { player, _, _, hitResult ->
            if (player.uuid == npc.id) {
                val blockInteractionMessage =
                    String.format(
                        "You used block at %s",
                        hitResult.blockPos,
                    )
                handleMessage(blockInteractionMessage)
            }
            return@register ActionResult.PASS
        }
    }
}
