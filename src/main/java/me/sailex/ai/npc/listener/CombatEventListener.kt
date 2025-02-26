package me.sailex.ai.npc.listener

import me.sailex.ai.npc.callback.PlayerDamageCallback
import me.sailex.ai.npc.model.NPC
import net.fabricmc.fabric.api.event.player.AttackEntityCallback
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.ActionResult

class CombatEventListener(
    npcs: Map<String, NPC>
) : AEventListener(npcs) {

    override fun register() {
        AttackEntityCallback.EVENT.register { player, _, _, entity, hitResult ->
            val matchingNpc = getMatchingNpc(player)
            if (matchingNpc != null) {
                var entityAttackMessage = if (hitResult == null) {
                    String.format("I tried to attacked entity %s, but you missed your hit", entity.name.string)
                } else {
                    String.format(
                        "I attacked entity %s at %s",
                        entity.name.string,
                        hitResult.pos,
                    )
                }
                matchingNpc.eventHandler.onEvent("user", entityAttackMessage)
            }
            return@register ActionResult.PASS
        }

        PlayerDamageCallback.EVENT.register { damageSource, amount ->
            val attacker = damageSource.attacker
            if (attacker is PlayerEntity) {

                val matchingNpc = getMatchingNpc(attacker)
                if (matchingNpc != null) {
                    val damageSourceMessage = String.format(
                        "I got damage: amount %s, type %s by Attacker %s",
                        amount, damageSource.type.msgId, damageSource.attacker?.name?.string
                    )
                    matchingNpc.eventHandler.onEvent("user", damageSourceMessage)
                }
            }
            return@register ActionResult.PASS
        }

    }
}
