package me.sailex.secondbrain.database.resources

import me.sailex.secondbrain.database.repositories.ConversationRepository
import me.sailex.secondbrain.database.repositories.RecipesRepository
import me.sailex.secondbrain.llm.LLMClient
import me.sailex.secondbrain.model.database.Conversation
import me.sailex.secondbrain.model.database.Recipe
import me.sailex.secondbrain.util.LogUtil
import me.sailex.secondbrain.util.ResourceRecommender

import net.minecraft.recipe.Ingredient
import net.minecraft.recipe.RecipeEntry
import net.minecraft.server.MinecraftServer
import net.minecraft.util.collection.DefaultedList
import java.sql.Timestamp
import java.util.concurrent.CompletableFuture

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class ResourcesProvider(
    private val conversationRepository: ConversationRepository,
    private val recipesRepository: RecipesRepository,
    val llmClient: LLMClient
) {
    private var executorService: ExecutorService = Executors.newFixedThreadPool(3)

    private val recipes = arrayListOf<Recipe>()
    private val conversations = arrayListOf<Conversation>()

    /**
     * Loads conversations recipes from db/mc into memory
     */
    fun loadResources(npcName: String, server: MinecraftServer) {
        runAsync {
            LogUtil.info("Loading resources into memory...")
            loadConversations(npcName)
            loadRecipes(server)
            executorService.shutdown()
        }
    }

    fun getRelevantRecipes(itemName: String): List<Recipe> {
        return ResourceRecommender.getRelevantResources(llmClient, itemName, recipes, 3)
            .filterIsInstance<Recipe>()
    }

    fun getRelevantConversations(message: String): List<Conversation> {
        return ResourceRecommender.getRelevantResources(llmClient, message, conversations, 3)
            .filterIsInstance<Conversation>()
    }

    fun addConversation(npcName: String, timestamp: Timestamp, message: String) {
        this.conversations.add(Conversation(
            npcName, message,
            timestamp,
            llmClient.generateEmbedding(listOf(message))))
    }

    /**
     * Saves recipes and conversations to local db. (called on server stop)
     *
     * Stops initial resources indexing if not finished by shutting down executor
     */
    fun saveResources() {
        shutdownServiceNow()
        executorService = Executors.newFixedThreadPool(2)
        val recipesFuture = runAsync {
            recipes.forEach { recipe -> recipesRepository.insert(recipe) }
            LogUtil.info("Saved recipes to db")
        }
        val conversationFuture = runAsync {
            conversations.forEach { conversation -> conversationRepository.insert(conversation) }
            LogUtil.info("Saved conversations to db")
        }
        CompletableFuture.allOf(recipesFuture, conversationFuture).get()
        executorService.shutdownNow()
    }

    private fun shutdownServiceNow() {
        if (!executorService.isTerminated) {
            executorService.shutdownNow()
            LogUtil.error("Initial loading of resources interrupted - Wait for termination")
            executorService.awaitTermination(500, TimeUnit.MILLISECONDS)
        }
    }

    private fun runAsync(task: () -> Unit): CompletableFuture<Void> {
        return CompletableFuture.runAsync(task , executorService).exceptionally {
            LogUtil.error("Error loading/saving resources into memory", it)
            null
        }
    }

    private fun loadConversations(npcName: String) {
        this.conversations.addAll(conversationRepository.selectByName(npcName))
    }

    private fun loadRecipes(server: MinecraftServer) {
        val recipeEntries: Collection<RecipeEntry<*>> = server.recipeManager.values().filter {
            it.value.ingredients.isNotEmpty()
        }

        //load from the db (avoid re-vectorize the recipe names)
        if (recipesRepository.selectCount() == recipeEntries.size) {
            this.recipes.addAll(recipesRepository.selectAll().filterIsInstance<Recipe>())
            return
        }

        recipeEntries.forEach { entry ->
            executorService.execute {
                this.recipes.add(buildRecipe(entry))
            }
        }
    }

    private fun buildRecipe(entry: RecipeEntry<*>): Recipe {
        val recipeName = entry.id.path
        val recipeValue = entry.value
        val ingredients = recipeValue.ingredients

        return Recipe(
            recipeName,
            recipeValue.type.toString(),
            recipeValue.createIcon().name.string,
            getItemNeeded(ingredients),
            llmClient.generateEmbedding(listOf(recipeName))
        )
    }

    private fun getItemNeeded(ingredients: DefaultedList<Ingredient>): String {
        return ingredients.filter { !it.matchingStacks.isNullOrEmpty() }
            .joinToString(",") { "${getItemId(it)}=${it.matchingStacks.size}" }
    }

    private fun getItemId(ingredient: Ingredient): String {
        val idString = ingredient.matchingStacks[0].name.toString()
        return idString.substring(idString.lastIndexOf(".") + 1, idString.lastIndexOf("'"))
    }
}
