package me.sailex.secondbrain.mode

import me.sailex.secondbrain.common.NPCController
import me.sailex.secondbrain.common.Tickable

class ModeController(
    private val controller: NPCController,
    private val nameToMode: Map<String, NPCMode>
): Tickable {
    /**
     * Executes active modes on game tick.
     * Modes are only executed when action queue is empty.
     */
    override fun onTick() {
        if (controller.isIdling) {
            nameToMode.values.filter { it.isOn }.forEach { it.onTick() }
        }
    }

    fun isOn(modeName: String): Boolean {
        val mode = nameToMode[modeName]
        if (mode == null) {
            return false
        }
        return mode.isOn
    }

    fun setAllIsOn(isOn: Boolean) {
        nameToMode.values.forEach { it.isOn = isOn }
    }

}
