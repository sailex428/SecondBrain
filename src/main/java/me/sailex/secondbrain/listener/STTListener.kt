package me.sailex.secondbrain.listener

import me.sailex.secondbrain.callback.STTCallback
import me.sailex.secondbrain.exception.LLMServiceException
import me.sailex.secondbrain.llm.player2.Player2APIClient
import me.sailex.secondbrain.model.NPC
import me.sailex.secondbrain.model.stt.STTType
import me.sailex.secondbrain.util.LogUtil
import java.util.UUID

class STTListener(npcs: Map<UUID, NPC>) : AEventListener(npcs) {

    override fun register() {
        STTCallback.EVENT.register { type ->
            npcs.forEach {
                performSTTAction(type, it.value)
            }
        }
    }

    private fun performSTTAction(type: STTType, npc: NPC) {
        try {
            if (npc.llmClient is Player2APIClient) {
                if (type == STTType.START) {
                    npc.llmClient.startSpeechToText()
                } else if (type == STTType.STOP) {
                    npc.eventHandler.onEvent(npc.llmClient.stopSpeechToText())
                }
            }
        } catch (e: LLMServiceException) {
            LogUtil.errorInChat("<" + npc.entity.name.string + ">" + e.message)
            LogUtil.error(e)
        }
    }
}