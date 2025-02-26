package me.sailex.ai.npc.listener

import me.sailex.ai.npc.model.NPC
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents

class ChatMessageListener(
    npcs: Map<String, NPC>
) : AEventListener(npcs) {

    override fun register() {
        ServerMessageEvents.CHAT_MESSAGE.register { message, sender, _ ->
            npcs.forEach { npcEntry ->
                if (npcEntry.key == sender.name.string) {
                    return@forEach
                }
                val chatMessage =
                    String.format(
                        "%s has written the message: %s",
                        sender.name.string ?: "Server Console",
                        message.content.string,
                    )
                npcEntry.value.eventHandler.onEvent("user", chatMessage)
            }
        }
    }
}
