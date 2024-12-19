package io.sailex.ai.npc.client.database.indexer

import io.sailex.ai.npc.client.AiNPCClient.client
import io.sailex.ai.npc.client.config.ResourceLoader
import io.sailex.ai.npc.client.database.repository.RequirementsRepository
import io.sailex.ai.npc.client.database.repository.TemplatesRepository
import io.sailex.ai.npc.client.llm.ILLMClient
import io.sailex.ai.npc.client.util.LogUtil

import net.minecraft.recipe.Recipe
import net.minecraft.recipe.RecipeEntry

class DefaultResourcesIndexer(
    val requirementsRepository: RequirementsRepository,
    val templatesRepository: TemplatesRepository,
    val llmClient: ILLMClient
) {

    fun indexTemplates() {
        ResourceLoader.getAllResourcesContent("template").forEach {
            templatesRepository.insert(
                it.key,
                it.value,
                llmClient.generateEmbedding(listOf(it.value))
            )
        }
    }

    /**
     * Indexes the requirements in db for all recipes in the game
     */
    //? if <1.21.2 {
    fun indexRequirements() {
        val world = client.world
        if (world == null) {
            LogUtil.error("Could not get 'recipes', cause the client world is null")
            return
        }
        val recipes: Collection<RecipeEntry<*>> = world.recipeManager.values()
        recipes.forEach {
            val recipeValue = it.value
            val recipeName = it.id.namespace
            requirementsRepository.insert(
                recipeValue.type.toString(),
                recipeName,
                llmClient.generateEmbedding(listOf(recipeName)),
                recipeValue.fits(2, 2),
                getBlocksNeeded(recipeValue)
            )
        }
    }

    private fun getBlocksNeeded(recipe: Recipe<*>): String {
        return recipe.ingredients.joinToString(",") { "${it.matchingStacks[0].name}=${it.matchingStacks.size}" }
    }
    //?}
}
