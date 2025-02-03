package me.sailex.ai.npc.listener

import me.sailex.ai.npc.npc.NPC
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents

class ChatMessageListener(
    npc: NPC
) : AEventListener(npc) {

    override fun register() {
        ServerMessageEvents.CHAT_MESSAGE.register { message, sender, _ ->
            if (sender.name.string.contains(npc.entity.name.string)) {
                return@register
            }
            val chatMessage =
                String.format(
                    "%s has written the message: %s",
                    sender.name.string ?: "Server Console",
                    message.content.string,
                )
            handleMessage(sender?.name?.string ?: "system", chatMessage)
        }
    }
}
