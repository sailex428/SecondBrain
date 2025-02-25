package me.sailex.ai.npc.listener

import me.sailex.ai.npc.model.NPC
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
        PlayerBlockBreakEvents.AFTER.register { _, player, pos, state, entity ->
            if (player.uuid != npc.entity.uuid) {
                return@register
            }
            val blockBreakMessage =
                String.format(
                    "You broke the block %s at %s",
                    state.block.name.string,
                    pos.toShortString(),
                )
            handleMessage("system", blockBreakMessage)
        }

        UseBlockCallback.EVENT.register { player, _, _, hitResult ->
            if (player.uuid == npc.entity.uuid) {
                val blockInteractionMessage =
                    String.format(
                        "You used block at %s",
                        hitResult.blockPos,
                    )
                handleMessage("system", blockInteractionMessage)
            }
            return@register ActionResult.PASS
        }
    }
}
