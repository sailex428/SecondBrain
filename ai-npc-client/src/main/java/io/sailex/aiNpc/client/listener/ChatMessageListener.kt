package io.sailex.aiNpc.client.listener

import io.sailex.aiNpc.client.model.NPC
import io.sailex.aiNpc.client.model.interaction.ActionType
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents

class ChatMessageListener(npc : NPC) : AEventListener(npc) {

    override fun register() {
        ClientReceiveMessageEvents.CHAT.register { message, signedMessage, sender, params, receptionTimestamp ->
            if (message.string.contains(npc.npcEntity.name.string)) {
                return@register
            }
            val chatMessage = String.format ("%S : %S has written the message: %S",
                receptionTimestamp.toString(), sender?.name ?: "Server Console", message.string)
            handleMessage(ActionType.CHAT, chatMessage)
        }
    }
}