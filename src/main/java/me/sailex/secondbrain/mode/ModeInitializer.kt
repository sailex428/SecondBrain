package me.sailex.secondbrain.mode

import me.sailex.secondbrain.common.NPCController
import me.sailex.secondbrain.constant.BlockConstants
import me.sailex.secondbrain.context.ContextProvider
import me.sailex.secondbrain.mixin.LivingEntityAccessor
import me.sailex.secondbrain.util.MCDataUtil.getBlockNameByPos
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.math.Direction

class ModeInitializer(
    private val entity: ServerPlayerEntity,
    private val controller: NPCController,
    private val contextProvider: ContextProvider
) {
    /**
     * Inits all modes that can be activated whilst NPC is idling.
     * @return mode names mapped to mode
     */
    fun initModes(): Map<String, NPCMode> {
        val modes = listOf<NPCMode>(
            cheatMode(),
            selfRelianceMode()
        )
        val nameToMode = hashMapOf<String, NPCMode>()
        modes.forEach { mode -> nameToMode.put(mode.name, mode) }
        return nameToMode
    }

    //TODO: Impl this mode
    private fun cheatMode(): NPCMode {
        return NPCMode.Builder().name("cheat").onTick {
            return@onTick
        }.build()
    }

    private fun selfRelianceMode(): NPCMode {
        return NPCMode.Builder()
            .name("self-reliance")
            .isOn(true)
            .onTick {
                //block the npc stands on
                val block = entity.world.getBlockState(entity.blockPos.offset(Direction.DOWN))
                        .block.name.string.lowercase()
                val blockAbove = getBlockNameByPos(entity)

                val goal = {
                    if (BlockConstants.WATER_BLOCKS.contains(blockAbove)) {
                        controller.jump()
                    } else if (BlockConstants.FALL_BLOCKS.any { blockAbove.contains(it) }) {
                        controller.chat("I'm falling!")
                        controller.moveAway()
                    } else if (BlockConstants.DANGEROUS_BLOCKS.any { block.contains(it) || blockAbove.contains(it) }) {
                        controller.chat("Im in danger!")

                        val nearestWater = contextProvider.cachedContext.findBlockByType("water")
                        if (nearestWater.isPresent) {
                            controller.moveToCoordinates(nearestWater.get().position())
                            controller.chat("I'm safe now!")
                        } else {
                            controller.moveAway()
                        }
                    } else if (System.currentTimeMillis() - (entity as LivingEntityAccessor).lastDamageTime < 3000 &&
                        (entity.health < 5 || entity.lastDamageTaken >= entity.health)) {
                        controller.chat("Im dying!")
                        controller.moveAway()
                    }
                }
                controller.addGoal("self-reliance", goal)
            }.build()
    }

    private fun farmingMode(): NPCMode {
        return NPCMode.Builder()
            .isOn(false)
            .name("farming").onTick {
                null
            }.build()
    }

}