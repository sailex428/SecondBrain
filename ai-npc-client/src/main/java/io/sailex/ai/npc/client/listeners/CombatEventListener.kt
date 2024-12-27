package io.sailex.ai.npc.client.listeners

import io.sailex.ai.npc.client.callback.PlayerDamageCallback
import io.sailex.ai.npc.client.model.NPC
import net.fabricmc.fabric.api.event.player.AttackEntityCallback
import net.minecraft.util.ActionResult

class CombatEventListener(
    npc: NPC
) : AEventListener(npc) {

    override fun register() {
        AttackEntityCallback.EVENT.register { player, _, _, entity, hitResult ->
            if (player.uuid == npc.id) {
                var entityAttackMessage = if (hitResult == null) {
                    String.format("You tried to attacked entity %s, but you missed your hit", entity.name.string)
                } else {
                    String.format(
                        "You attacked entity %s at %s",
                        entity.name.string,
                        hitResult.pos,
                    )
                }
                handleMessage(entityAttackMessage)
            }
            return@register ActionResult.PASS
        }

        PlayerDamageCallback.EVENT.register { attacker, damageName, pos ->
            if (attacker != null) {
                val damageSourceMessage = String.format(
                    "You got damage of type %s by Attacker %s at %s", damageName, attacker.name.string, pos,
                )
                handleMessage(damageSourceMessage)
            }
            return@register ActionResult.PASS
        }

    }
}
