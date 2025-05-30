package me.sailex.secondbrain.listener

import me.sailex.secondbrain.callback.PlayerDamageCallback
import me.sailex.secondbrain.model.NPC
import net.fabricmc.fabric.api.event.player.AttackEntityCallback
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.ActionResult
import java.util.UUID

class CombatEventListener(
    npcs: Map<UUID, NPC>
) : AEventListener(npcs) {

    override fun register() {
        AttackEntityCallback.EVENT.register { player, _, _, entity, hitResult ->
            if (entity is PlayerEntity) {
                val matchingNpc = getMatchingNpc(entity)
                if (matchingNpc != null) {
                    var entityAttackMessage = if (hitResult == null) {
                        String.format("You tried to attacked %s, but you missed the hit", player.name.string)
                    } else {
                        String.format(
                            "I attacked entity %s at %s",
                            player.name.string,
                            hitResult.pos,
                        )
                    }
//                    matchingNpc.eventHandler.onEvent(entityAttackMessage)
                }
            }
            return@register ActionResult.PASS
        }

        PlayerDamageCallback.EVENT.register { damageSource, victim, amount ->
            val matchingNpc = getMatchingNpc(victim)
            if (matchingNpc != null) {
                val damageSourceMessage = String.format(
                    "You got damage: amount %s by %s",
                    amount, damageSource.attacker?.name?.string
                )
                matchingNpc.eventHandler.onEvent(damageSourceMessage)
            }
            return@register ActionResult.PASS
        }

    }
}
