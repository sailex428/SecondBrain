package io.sailex.ai.npc.client.database.indexer

import io.sailex.ai.npc.client.AiNPCClient.client
import io.sailex.ai.npc.client.config.ResourceLoader
import io.sailex.ai.npc.client.database.repositories.RequirementsRepository
import io.sailex.ai.npc.client.database.repositories.TemplatesRepository
import io.sailex.ai.npc.client.llm.ILLMClient
import io.sailex.ai.npc.client.util.LogUtil

import net.minecraft.recipe.Ingredient
import net.minecraft.recipe.Recipe
import net.minecraft.recipe.RecipeEntry

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class DefaultResourcesIndexer(
    val requirementsRepository: RequirementsRepository,
    val templatesRepository: TemplatesRepository,
    val llmClient: ILLMClient
) {
    val logger: Logger = LogManager.getLogger(this.javaClass)

    /**
     * Indexes the templates set in resources/templates dir
     */
    fun indexTemplates() {
        logger.info("Indexing all Templates")
        ResourceLoader.getAllResourcesContent("template").forEach {
            templatesRepository.insert(
                it.key,
                it.value,
                llmClient.generateEmbedding(listOf(it.value))
            )
        }
        logger.info("Finished indexing all Templates")
    }

    /**
     * Indexes the requirements/recipes of the game in db
     */
    //? if <1.21.2 {
    fun indexRequirements() {
        val world = client.world
        if (world == null) {
            LogUtil.error("Could not get 'recipes', cause the client world is null")
            return
        }
        val recipes: Collection<RecipeEntry<*>> = world.recipeManager.values()
        val executorService: ExecutorService = Executors.newFixedThreadPool(6)

        logger.info("Indexing all Requirements and Recipes")
        recipes.forEach {
            executorService.submit {
                val recipeValue = it.value
                val recipeName = it.id.path
                requirementsRepository.insert(
                    recipeValue.type.toString(),
                    recipeName,
                    llmClient.generateEmbedding(listOf(recipeName)),
                    recipeValue.fits(2, 2),
                    getItemNeeded(recipeValue)
                )
            }
        }
        executorService.shutdown()
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
