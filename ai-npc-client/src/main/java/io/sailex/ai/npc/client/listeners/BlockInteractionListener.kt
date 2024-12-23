package io.sailex.ai.npc.client.listeners

import io.sailex.ai.npc.client.model.NPC
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents
import net.fabricmc.fabric.api.event.player.UseBlockCallback
import net.minecraft.util.ActionResult

/**
 * Listens to block interactions of this npc
 */
class BlockInteractionListener(
    npc: NPC,
) : AEventListener(npc) {
    override fun register() {
        PlayerBlockBreakEvents.AFTER.register { _, player, pos, state, _ ->
            if (player.uuid != npc.id) {
                return@register
            }
            val blockBreakMessage =
                String.format(
                    "You broke the block %S at %S",
                    state.block.name.string,
                    pos.toShortString(),
                )

            logger.info(blockBreakMessage)
            handleMessage(blockBreakMessage)
        }

        UseBlockCallback.EVENT.register { player, _, _, hitResult ->
            if (player.uuid == npc.id) {
                val blockInteractionMessage =
                    String.format(
                        "You used block at %S",
                        hitResult.blockPos,
                    )

                logger.info(blockInteractionMessage)
                handleMessage(blockInteractionMessage)
            }
            return@register ActionResult.PASS
        }
    }
}
