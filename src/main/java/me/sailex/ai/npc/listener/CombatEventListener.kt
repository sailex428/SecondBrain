package me.sailex.ai.npc.listener

import me.sailex.ai.npc.callback.PlayerDamageCallback
import me.sailex.ai.npc.model.NPC
import net.fabricmc.fabric.api.event.player.AttackEntityCallback
import net.minecraft.util.ActionResult

class CombatEventListener(
    npc: NPC
) : AEventListener(npc) {

    override fun register() {
        AttackEntityCallback.EVENT.register { player, _, _, entity, hitResult ->
            if (player.uuid == npc.entity.uuid) {
                var entityAttackMessage = if (hitResult == null) {
                    String.format("I tried to attacked entity %s, but you missed your hit", entity.name.string)
                } else {
                    String.format(
                        "I attacked entity %s at %s",
                        entity.name.string,
                        hitResult.pos,
                    )
                }
                handleMessage("system", entityAttackMessage)
            }
            return@register ActionResult.PASS
        }

        PlayerDamageCallback.EVENT.register { damageSource, amount ->
            if (damageSource.attacker != null && damageSource.attacker?.uuid != npc.entity.uuid) {
                val damageSourceMessage = String.format(
                    "I got damage: amount %s, type %s by Attacker %s",
                        amount, damageSource.type.msgId, damageSource.attacker?.name?.string
                )
                handleMessage("system", damageSourceMessage)
            }
            return@register ActionResult.PASS
        }

    }
}
