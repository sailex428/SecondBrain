package me.sailex.ai.npc.listener

import me.sailex.ai.npc.callback.PlayerDamageCallback
import me.sailex.ai.npc.model.NPC
import net.minecraft.util.ActionResult

class CombatEventListener(
    npc: NPC
) : AEventListener(npc) {

    override fun register() {
//        AttackEntityCallback.EVENT.register { player, _, _, entity, hitResult ->
//            if (player.uuid == npc.entity.uuid) {
//                var entityAttackMessage = if (hitResult == null) {
//                    String.format("You tried to attacked entity %s, but you missed your hit", entity.name.string)
//                } else {
//                    String.format(
//                        "You attacked entity %s at %s",
//                        entity.name.string,
//                        hitResult.pos,
//                    )
//                }
//                handleMessage(entityAttackMessage)
//            }
//            return@register ActionResult.PASS
//        }

        PlayerDamageCallback.EVENT.register { attacker, target, damageName, pos ->
            if (attacker != null && target.uuid == npc.entity.uuid) {
                val damageSourceMessage = String.format(
                    "You got damage of type %s by Attacker %s at %s", damageName, attacker.name.string, pos,
                )
                handleMessage("system", damageSourceMessage)
            }
            return@register ActionResult.PASS
        }

    }
}
