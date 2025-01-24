package me.sailex.ai.npc.llm.function_calling

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyDescription
import io.github.sashirestela.openai.common.function.FunctionDef
import io.github.sashirestela.openai.common.function.FunctionExecutor
import io.github.sashirestela.openai.common.function.Functional
import me.sailex.ai.npc.context.ContextGenerator
import me.sailex.ai.npc.model.context.WorldContext
import me.sailex.ai.npc.npc.NPCController
import me.sailex.ai.npc.npc.NPCInteraction
import net.minecraft.server.network.ServerPlayerEntity

class OpenAiFunctionManager(
    val controller: NPCController,
    val npcEntity: ServerPlayerEntity
): IFunctionManager {
    override var functionExecutor = FunctionExecutor()

    init {
        val functions = listOf<FunctionDef>(
            defineFunction("chat", "Print message into game chat.", Chat::class.java),
            defineFunction("move", "Move to the given location", Move::class.java),
            defineFunction("mine", "Mine the block at the given location", Mine::class.java),
            defineFunction("drop", "Drop one item of the given inventory slot", Drop::class.java),
            defineFunction("dropAll", "Drop all items of the given inventory slot", DropAll::class.java),
            defineFunction("attack", "Attack the entity of the given entity id", Attack::class.java),
            defineFunction("getEntities", "Get all entities next to the npc player", GetEntities::class.java),
            defineFunction("getBlocks", "Get all blocks next to the player", GetBlocks::class.java),
            defineFunction("getNpcState", "Get npc state (foodlevel, health, ...) and inventory items", GetNpcState::class.java),
            defineFunction("stop", "Stop all running npc actions (Should only be used if expressly requested)", Stop::class.java)
        )
        functions.forEach(functionExecutor::enrollFunction)
    }

    fun <T: Functional> defineFunction(name: String, description: String, clazz: Class<T>): FunctionDef {
        return FunctionDef.builder()
            .name(name)
            .description(description)
            .functionalClass(clazz)
            .strict(true)
            .build()
    }

    inner class Chat(): Functional {

        @JsonPropertyDescription("Represents the message that will be printed in the game chat.")
        @JsonProperty(required = true)
        val message: String = ""

        override fun execute(): Any? {
            controller.chat(message)
            return "chat message $message"
        }

    }

    inner class Move(): Functional {

        @JsonProperty(required = true)
        val x: Int = 0

        @JsonProperty(required = true)
        val y: Int = 0

        @JsonProperty(required = true)
        val z: Int = 0

        override fun execute(): Any? {
            controller.move(WorldContext.Position(x, y, z))
            return "move to $x, $y, $z"
        }
    }

    inner class Mine(): Functional {

        @JsonProperty(required = true)
        val x: Int = 0

        @JsonProperty(required = true)
        val y: Int = 0

        @JsonProperty(required = true)
        val z: Int = 0

        override fun execute(): Any? {
            controller.mine(WorldContext.Position(x, y, z))
            return "mine block at $x, $y, $z"
        }
    }

    inner class Drop(): Functional {

        @JsonPropertyDescription("slot number of the item in the inventory")
        @JsonProperty(required = true)
        val slot: Int = 0

        override fun execute(): Any? {
            controller.drop(slot)
            return "drops one item from slot $slot"
        }
    }

    inner class DropAll(): Functional {

        @JsonPropertyDescription("slot number of the item in the inventory")
        @JsonProperty(required = true)
        val slot: Int = 0

        override fun execute(): Any? {
            controller.drop(slot)
            return "drops all items from slot $slot"
        }
    }

    inner class Attack(): Functional {

        @JsonPropertyDescription("entity id of the Entity that is to be attacked")
        @JsonProperty(required = true)
        val entityId: Int = 0

        override fun execute(): Any? {
            controller.attack(entityId)
            return "tries to attack the entity $entityId"
        }
    }

    inner class Stop(): Functional {
        override fun execute(): Any? {
            controller.cancelActions()
            return "stop all actions"
        }
    }

    inner class GetEntities(): Functional {
        override fun execute(): Any? {
            return NPCInteraction.formatEntities(ContextGenerator.scanNearbyEntities(npcEntity))
        }
    }

    inner class GetBlocks(): Functional {
        override fun execute(): Any? {
            return NPCInteraction.formatBlocks(ContextGenerator.scanNearbyBlocks(npcEntity))
        }
    }

    inner class GetNpcState(): Functional {
        override fun execute(): Any? {
            return NPCInteraction.formatInventory(ContextGenerator.getInventoryState(npcEntity)) +
                    NPCInteraction.formatNPCState(ContextGenerator.getNpcState(npcEntity))
        }
    }

}
