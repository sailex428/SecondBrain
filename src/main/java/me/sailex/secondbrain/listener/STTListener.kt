package me.sailex.secondbrain.listener

import me.sailex.secondbrain.callback.STTCallback
import me.sailex.secondbrain.event.EventHandler
import me.sailex.secondbrain.exception.LLMServiceException
import me.sailex.secondbrain.llm.LLMClient
import me.sailex.secondbrain.llm.player2.Player2APIClient
import me.sailex.secondbrain.model.NPC
import me.sailex.secondbrain.model.stt.STTType
import me.sailex.secondbrain.util.LogUtil
import java.util.UUID

class STTListener(npcs: Map<UUID, NPC>) : AEventListener(npcs) {

    override fun register() {
        STTCallback.EVENT.register { type ->
            npcs.forEach {
                performSTTAction(type, it.value.llmClient, it.value.eventHandler)
            }
        }
    }

    private fun performSTTAction(type: STTType, llmClient: LLMClient, eventHandler: EventHandler) {
        try {
            if (llmClient is Player2APIClient) {
                if (type == STTType.START) {
                    llmClient.startSpeechToText()
                } else if (type == STTType.STOP) {
                    eventHandler.onEvent(llmClient.stopSpeechToText())
                }
            }
        } catch (e: LLMServiceException) {
            LogUtil.errorInChat(e.message)
            LogUtil.error(e)
        }
    }
}