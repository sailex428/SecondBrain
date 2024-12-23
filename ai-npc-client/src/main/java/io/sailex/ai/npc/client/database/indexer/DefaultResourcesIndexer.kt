package io.sailex.ai.npc.client.database.indexer

import io.sailex.ai.npc.client.AiNPCClient.client
import io.sailex.ai.npc.client.config.ResourceLoader.getAllResourcesContent
import io.sailex.ai.npc.client.database.repositories.ActionsRepository
import io.sailex.ai.npc.client.database.repositories.BlockRepository
import io.sailex.ai.npc.client.database.repositories.RecipesRepository
import io.sailex.ai.npc.client.llm.ILLMClient
import io.sailex.ai.npc.client.util.ActionParser.parseSingleAction
import io.sailex.ai.npc.client.util.ClientWorldUtil.getMiningLevel
import io.sailex.ai.npc.client.util.ClientWorldUtil.getToolNeeded
import io.sailex.ai.npc.client.util.LogUtil

import net.minecraft.recipe.Ingredient
import net.minecraft.recipe.Recipe
import net.minecraft.recipe.RecipeEntry
import net.minecraft.registry.Registries

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class DefaultResourcesIndexer(
    val recipesRepository: RecipesRepository,
    val actionsRepository: ActionsRepository,
    val blockRepository: BlockRepository,
    val llmClient: ILLMClient
) {
    val logger: Logger = LogManager.getLogger(this.javaClass)
    val executorService: ExecutorService = Executors.newFixedThreadPool(6)

    fun indexAll() {
        indexExampleActions()
        indexBlocks()
        //? if <1.21.2
        indexRecipes()
        executorService.shutdown()
    }

    /**
     * Indexes the actions set in resources/actions-examples dir
     */
    private fun indexExampleActions() {
        logger.info("Indexing all example Actions")
        getAllResourcesContent("actions-examples").forEach {
            executorService.submit {
                val action = parseSingleAction(it.value)
                actionsRepository.insert(
                    it.key,
                    action.message,
                    llmClient.generateEmbedding(listOf(action.message)),
                    it.value
                )
            }
        }
    }

    private fun indexBlocks() {
        logger.info("Indexing all Blocks")
        Registries.BLOCK.forEach { block -> {
            executorService.submit {
                val name = block.translationKey.toString()
                val blockState = block.stateManager.defaultState
                blockRepository.insert(
                    Registries.BLOCK.getId(block).namespace,
                    name,
                    llmClient.generateEmbedding(listOf(name)),
                    getMiningLevel(blockState),
                    getToolNeeded(blockState)
                )
            }
        }}
    }

    /**
     * Indexes the requirements/recipes of the game in db
     */
    //? if <1.21.2 {
    private fun indexRecipes() {
        val world = client.world
        if (world == null) {
            LogUtil.error("Could not get 'recipes', cause the client world is null")
            return
        }
        val recipes: Collection<RecipeEntry<*>> = world.recipeManager.values()

        logger.info("Indexing all Recipes")
        recipes.forEach {
            executorService.submit {
                val recipeValue = it.value
                val recipeName = it.id.path
                recipesRepository.insert(
                    recipeValue.type.toString(),
                    recipeName,
                    llmClient.generateEmbedding(listOf(recipeName)),
                    recipeValue.createIcon().name.string,
                    getItemNeeded(recipeValue)
                )
            }
        }
    }

    private fun getItemNeeded(recipe: Recipe<*>): String {
        return recipe.ingredients.joinToString(",") {
            "${getItemId(it)}=${it.matchingStacks.size}"
        }
    }

    private fun getItemId(ingredient: Ingredient): String? {
        val itemStack = ingredient.matchingStacks
        if (itemStack.isNullOrEmpty()) {
            return null
        }
        val idString = ingredient.matchingStacks[0].name.toString()
        return idString.substring(idString.indexOf("'"), idString.lastIndexOf("'"))
    }
    //?}
}
