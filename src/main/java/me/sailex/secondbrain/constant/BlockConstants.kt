package me.sailex.secondbrain.constant

/**
 * Contains constants for various block categories used throughout the NPC system.
 */
object BlockConstants {

    /**
     * Blocks that can fall and potentially harm the NPC
     */
    val FALL_BLOCKS = listOf(
        "sand",
        "gravel",
        "concrete_powder"
    )

    /**
     * Blocks that can cause damage to the NPC
     */
    val DANGEROUS_BLOCKS = listOf(
        "lava",
        "flowing_lava",
        "fire",
        "magma_block",
        "cactus",
        "campfire",
        "soul_fire",
        "sweet_berry_bush",
        "wither_rose"
    )

    /**
     * Blocks containing water that can extinguish fire
     */
    val WATER_BLOCKS = listOf(
        "water",
        "flowing_water"
    )

    val BLOCKS_TO_FARM = listOf(
        "dirt",
        "oak_log",
        "birch_log"
    )

}