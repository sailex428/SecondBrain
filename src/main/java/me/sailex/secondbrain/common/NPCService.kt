package me.sailex.secondbrain.common

import me.sailex.altoclef.multiversion.EntityVer
import me.sailex.secondbrain.auth.UsernameValidator
import me.sailex.secondbrain.callback.NPCEvents
import me.sailex.secondbrain.config.ConfigProvider
import me.sailex.secondbrain.config.NPCConfig
import me.sailex.secondbrain.constant.Instructions
import me.sailex.secondbrain.database.resources.ResourceProvider
import me.sailex.secondbrain.exception.NPCCreationException
import me.sailex.secondbrain.model.NPC
import me.sailex.secondbrain.util.LogUtil
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.MinecraftServer
import net.minecraft.server.PlayerManager
import net.minecraft.util.math.BlockPos
import java.util.UUID
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class NPCService(
    private val factory: NPCFactory,
    private val configProvider: ConfigProvider,
    private val resourceProvider: ResourceProvider
) {
    companion object {
        private const val MAX_NUMBER_OF_NPC = 10
    }

    private lateinit var executorService: ExecutorService
    val uuidToNpc = ConcurrentHashMap<UUID, NPC>()

    fun init() {
        executorService = Executors.newSingleThreadExecutor()
    }

    fun createNpc(newConfig: NPCConfig, server: MinecraftServer, spawnPos: BlockPos?, owner: PlayerEntity?) {
        CompletableFuture.runAsync({
            val name = newConfig.npcName
            checkLimit()
            checkNpcName(name)

            val config = updateConfig(newConfig)

            NPCSpawner.spawn(config, server, spawnPos) { npcEntity ->
                config.uuid = npcEntity.uuid
                val npc = factory.createNpc(npcEntity, config, resourceProvider.loadedConversations[config.uuid])
                npc.controller.owner = owner
                uuidToNpc[config.uuid] = npc

                LogUtil.infoInChat(("Added NPC with name: $name"))
                npc.eventHandler.onEvent(Instructions.INITIAL_PROMPT)
            }

            NPCEvents.ON_DEATH.register {
                removeNpc(it.uuid, EntityVer.getWorld(it).server!!.playerManager)
            }
        }, executorService).exceptionally {
            LogUtil.errorInChat(it.message)
            LogUtil.error(it)
            null
        }
    }

    fun removeNpc(uuid: UUID, playerManager: PlayerManager) {
        val npcToRemove = uuidToNpc[uuid]
        if (npcToRemove != null) {
            npcToRemove.controller.stop()
            npcToRemove.llmClient.stopService()
            npcToRemove.eventHandler.stopService()
            npcToRemove.contextProvider.chunkManager.stopService()
            resourceProvider.addConversations(uuid,npcToRemove.history.latestConversations)
            uuidToNpc.remove(uuid)

            NPCSpawner.remove(npcToRemove.entity.uuid, playerManager)

            val config = configProvider.getNpcConfig(uuid)
            if (config.isPresent) {
                config.get().isActive = false
                LogUtil.infoInChat("Removed NPC with name ${config.get().npcName}")
            } else {
                LogUtil.infoInChat("Removed NPC with uuid $uuid")
            }
        }
    }

    fun deleteNpc(uuid: UUID, playerManager: PlayerManager) {
        resourceProvider.loadedConversations.remove(uuid)
        resourceProvider.conversationRepository.deleteByUuid(uuid)
        removeNpc(uuid, playerManager)
        configProvider.deleteNpcConfig(uuid)
    }

    fun shutdownNPCs(server: MinecraftServer) {
        uuidToNpc.keys.forEach {
            removeNpc(it, server.playerManager)
        }
        executorService.shutdownNow()
    }

    private fun updateConfig(newConfig: NPCConfig): NPCConfig {
        val config = configProvider.getNpcConfigByName(newConfig.npcName)
        if (config.isEmpty) {
            return configProvider.addNpcConfig(newConfig)
        } else {
            config.get().isActive = true
            return config.get()
        }
    }

    private fun checkNpcName(npcName: String) {
        if (!UsernameValidator.isValid(npcName)) {
            throw NPCCreationException("NPC name is not valid. Use 3–16 characters: letters, numbers, or underscores only.")
        } else if (uuidToNpc.values.any { it.entity.name.string == npcName }) {
            throw NPCCreationException("A NPC with the name '$npcName' already exists.")
        }
    }

    private fun checkLimit() {
        if (uuidToNpc.size == MAX_NUMBER_OF_NPC) {
            throw NPCCreationException("Currently there are no more than" + MAX_NUMBER_OF_NPC +" parallel running " +
                    "NPCs supported!")
        }
    }

}