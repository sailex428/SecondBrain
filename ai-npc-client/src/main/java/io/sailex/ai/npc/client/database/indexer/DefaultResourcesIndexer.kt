package io.sailex.ai.npc.client.database.indexer

import com.fasterxml.jackson.databind.ObjectMapper
import io.sailex.ai.npc.client.AiNPCClient.client
import io.sailex.ai.npc.client.config.ResourceLoader.getAllResourcesContent
import io.sailex.ai.npc.client.database.repositories.SkillRepository
import io.sailex.ai.npc.client.database.repositories.BlockRepository
import io.sailex.ai.npc.client.database.repositories.RecipesRepository
import io.sailex.ai.npc.client.llm.ILLMClient
import io.sailex.ai.npc.client.util.LogUtil

import net.minecraft.recipe.Ingredient
import net.minecraft.recipe.RecipeEntry
import net.minecraft.registry.Registries
import net.minecraft.util.collection.DefaultedList

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
    val mapper = ObjectMapper()

    fun indexAll() {
        indexExampleSkills()
        indexBlocks()
    }

    /**
     * Indexes the skills set in resources/actions-examples dir
     */
    private fun indexExampleSkills() {
        val exampleSkills = getAllResourcesContent("skill-examples")
        if (isAlreadyIndexed(exampleSkills.size, skillRepository.selectCount())) return

        logger.info("Indexing all example Skills")
        exampleSkills.forEach {
            indexAsync {
                val cleanedJson = removeWhiteSpaces(it.value)
                skillRepository.insert(
                    it.key,
                    cleanedJson,
                    llmClient.generateEmbedding(listOf(cleanedJson)),
                )
            }
        }
    }

    /**
     * Indexes all blocks - identifier, name
     */
    private fun indexBlocks() {
        val blocks = Registries.BLOCK
        if (isAlreadyIndexed(blocks.size(), blockRepository.selectCount())) return

        logger.info("Indexing all Blocks")
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
        val recipes: Collection<RecipeEntry<*>> = world.recipeManager.values().filter { !it.value.ingredients.isEmpty() }
        if (isAlreadyIndexed(recipes.size, recipesRepository.selectCount())) return

        logger.info("Indexing all Recipes")
        recipes.forEach {
            val recipeValue = it.value
            val ingredients = recipeValue.ingredients
            indexAsync {
                val recipeName = it.id.path
                recipesRepository.insert(
                    recipeValue.type.toString(),
                    recipeName,
                    llmClient.generateEmbedding(listOf(recipeName)),
                    recipeValue.createIcon().name.string,
                    getItemNeeded(ingredients)
                )
            }
        }
    }

    private fun getItemNeeded(ingredients: DefaultedList<Ingredient>): String {
        return ingredients.filter {
            getItemId(it) != null
        }.joinToString(",") {
            "${getItemId(it)}=${it.matchingStacks.size}"
        }
    }

    private fun getItemId(ingredient: Ingredient): String? {
        val itemStack = ingredient.matchingStacks
        if (itemStack.isNullOrEmpty()) {
            return null
        }
        val idString = ingredient.matchingStacks[0].name.toString()
        return idString.substring(idString.indexOf("'") + 1, idString.lastIndexOf("'"))
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

    //TODO: maybe use gson?
    private fun removeWhiteSpaces(content: String): String {
        return mapper.writeValueAsString(mapper.readTree(content))
    }

    private fun isAlreadyIndexed(expected: Int, indexed: Int): Boolean {
        return indexed >= expected
    }

}
