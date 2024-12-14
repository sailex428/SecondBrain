package io.sailex.aiNpc.client.listener

import io.sailex.aiNpc.client.model.NPC
import io.sailex.aiNpc.client.model.interaction.ActionType
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents
import net.fabricmc.fabric.api.event.player.UseBlockCallback
import net.minecraft.util.ActionResult

/**
 * Listens to block interactions of this npc
 */
class BlockInteractionListener(npc: NPC) : AEventListener(npc) {

    override fun register() {
        PlayerBlockBreakEvents.AFTER.register { world, player, pos, state, blockEntity ->
            if (player.uuid != npc.id) {
                return@register
            }
            val blockBreakMessage = String.format("You broke the block %S at %S",
                state.block.name.string, pos.toShortString())

            logger.info(blockBreakMessage)
            handleMessage(ActionType.MINE, blockBreakMessage)
        }

        UseBlockCallback.EVENT.register { player, world, hand, hitResult ->
            if (player.uuid == npc.id) {
                val blockInteractionMessage = String.format(
                    "Player %S interacted with block at %S",
                    player.name.string, hitResult.blockPos.toShortString()
                )

                logger.info(blockInteractionMessage)
                handleMessage(ActionType.INTERACT, blockInteractionMessage)
            }
            return@register ActionResult.PASS
        }
    }

}