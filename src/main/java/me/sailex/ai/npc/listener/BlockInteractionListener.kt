package me.sailex.ai.npc.listener

import me.sailex.ai.npc.npc.NPC
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
//        ClientPlayerBlockBreakEvents.AFTER.register { _, player, pos, state ->
//            if (player.uuid != npc.entity.uuid) {
//                return@register
//            }
//            val blockBreakMessage =
//                String.format(
//                    "You broke the block %s at %s",
//                    state.block.name.string,
//                    pos.toShortString(),
//                )
//            handleMessage(blockBreakMessage)
//        }
//
//        UseBlockCallback.EVENT.register { player, _, _, hitResult ->
//            if (player.uuid == npc.entity.uuid) {
//                val blockInteractionMessage =
//                    String.format(
//                        "You used block at %s",
//                        hitResult.blockPos,
//                    )
//                handleMessage(blockInteractionMessage)
//            }
//            return@register ActionResult.PASS
//        }
    }
}
