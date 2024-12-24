package io.sailex.ai.npc.client.database.index

import io.sailex.ai.npc.client.AiNPCClient.client
import io.sailex.ai.npc.client.config.ResourceLoader.getAllResourcesContent
import io.sailex.ai.npc.client.database.repositories.SkillRepository
import io.sailex.ai.npc.client.database.repositories.BlockRepository
import io.sailex.ai.npc.client.database.repositories.RecipesRepository
import io.sailex.ai.npc.client.llm.ILLMClient
import io.sailex.ai.npc.client.util.LogUtil

import net.minecraft.recipe.Ingredient
import net.minecraft.recipe.Recipe
import net.minecraft.recipe.RecipeEntry
import net.minecraft.registry.Registries

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.util.concurrent.CompletableFuture

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class DefaultResourcesIndexer(
    val recipesRepository: RecipesRepository,
    val skillRepository: SkillRepository,
    val blockRepository: BlockRepository,
    val llmClient: ILLMClient
) {
    val logger: Logger = LogManager.getLogger(this.javaClass)
    val executorService: ExecutorService = Executors.newFixedThreadPool(6)

    fun indexAll() {
        indexExampleSkills()
        indexBlocks()
    }

    /**
     * Indexes the actions set in resources/actions-examples dir
     */
    private fun indexExampleSkills() {
        logger.info("Indexing all example Skills")
        getAllResourcesContent("skill-examples").forEach {
            indexAsync {
                skillRepository.insert(
                    it.key,
                    it.value,
                    llmClient.generateEmbedding(listOf(it.value)),
                )
            }
        }
    }

    /**
     * Indexes all blocks - identifier, name
     */
    private fun indexBlocks() {
        logger.info("Indexing all Blocks")
        val blocks = Registries.BLOCK
        blocks.forEach {
            indexAsync {
                val name = it.translationKey.toString()
                blockRepository.insert(
                    Registries.BLOCK.getId(it).path,
                    name,
                    llmClient.generateEmbedding(listOf(name))
                )
            }
        }
    }

    /**
     * Indexes the requirements/recipes of the game in db
     */
    //? if <1.21.2 {
    fun indexRecipes() {
        val world = client.world
        if (world == null) {
            LogUtil.error("Could not get 'recipes', cause the client world is null")
            return
        }
        val recipes: Collection<RecipeEntry<*>> = world.recipeManager.values()

        logger.info("Indexing all Recipes")
        recipes.forEach {
            indexAsync {
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

    private fun indexAsync(function:() -> Unit) {
        CompletableFuture.runAsync(function, executorService).exceptionally {
            logger.error("Error while running async indexing task", it)
            null
        }
    }

    fun shutdownExecutor() {
        executorService.shutdown()
    }
}
