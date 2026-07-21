package me.sailex.secondbrain.listener

import me.sailex.secondbrain.model.NPC
import java.util.UUID

abstract class AEventListener(
    protected val npcs: Map<UUID, NPC>
) : IEventListener
