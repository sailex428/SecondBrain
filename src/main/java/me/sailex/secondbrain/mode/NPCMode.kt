package me.sailex.secondbrain.mode

/**
 * Represents a mode that will be used when NPC is idling / executing action.
 */
class NPCMode(
    val name: String,
    var isOn: Boolean,
    val onTick: () -> Unit
) {
    private constructor(builder: Builder) : this(builder.name, builder.isOn, builder.onTick)

    class Builder {
        var name: String = ""
            private set

        var isOn: Boolean = false
            private set
        
        var onTick: () -> Unit = {}
            private set

        fun name(name: String) = apply { this.name = name }
        fun isOn(isOn: Boolean) = apply { this.isOn = isOn }
        fun onTick(onTick: () -> Unit) = apply { this.onTick = onTick }

        fun build() = NPCMode(this)
    }

}