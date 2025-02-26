package me.sailex.ai.npc.listener

import me.sailex.ai.npc.model.NPC
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents
import net.fabricmc.fabric.api.event.player.UseBlockCallback
import net.minecraft.util.ActionResult

/**
 * Listens to block interactions of this npc
 */
class BlockInteractionListener(
    npcs: Map<String, NPC>
) : AEventListener(npcs) {
    override fun register() {
        PlayerBlockBreakEvents.AFTER.register { _, player, pos, state, _ ->
            val matchingNpc = getMatchingNpc(player)
            if (matchingNpc != null) {
                val blockBreakMessage =
                    String.format(
                        "I broke the block %s at %s",
                        state.block.name.string,
                        pos.toShortString(),
                    )
                matchingNpc.eventHandler.onEvent("user", blockBreakMessage)
            }
        }

        UseBlockCallback.EVENT.register { player, _, _, hitResult ->
            val matchingNpc = getMatchingNpc(player)
            if (matchingNpc != null) {
                val blockInteractionMessage =
                    String.format(
                        "I used block at %s",
                        hitResult.blockPos,
                    )
                matchingNpc.eventHandler.onEvent("user", blockInteractionMessage)
            }
            return@register ActionResult.PASS
        }
    }
}
