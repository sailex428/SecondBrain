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
            val llmClient = npcs.map { it.value.llmClient }.filterIsInstance<Player2APIClient>().firstOrNull()
            if (llmClient != null) {
                performSTTAction(type, llmClient)
            } else {
                LogUtil.errorInChat("No NPC found that uses Player2.")
            }
        }
    }

    private fun performSTTAction(type: STTType, llmClient: Player2APIClient) {
        try {
            if (type == STTType.START) {
                llmClient.startSpeechToText()
            } else if (type == STTType.STOP) {
                callEventForPlayer2Npcs(llmClient.stopSpeechToText())
            }
        } catch (e: LLMServiceException) {
            LogUtil.errorInChat(e.message)
            LogUtil.error(e)
        }
    }

    private fun callEventForPlayer2Npcs(prompt: String) {
        npcs.forEach {
            if (it.value.llmClient is Player2APIClient) {
                it.value.eventHandler.onEvent(prompt)
            }
        }
    }
}