package me.sailex.secondbrain.listener

import me.sailex.secondbrain.model.NPC
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents
import net.fabricmc.fabric.api.event.player.UseBlockCallback
import net.minecraft.util.ActionResult
import java.util.UUID

/**
 * @deprecated Will be removed, cause this layer doesnt handle the llm anymore
 * Listens to block interactions of this npc
 */
class BlockInteractionListener(
    npcs: Map<UUID, NPC>
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
//                matchingNpc.eventHandler.onEvent(blockBreakMessage)
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
//                matchingNpc.eventHandler.onEvent(blockInteractionMessage)
            }
            return@register ActionResult.PASS
        }
    }
}
