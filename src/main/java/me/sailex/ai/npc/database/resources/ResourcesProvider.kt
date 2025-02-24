package me.sailex.ai.npc.database.resources

import me.sailex.ai.npc.database.repositories.ConversationRepository
import me.sailex.ai.npc.database.repositories.RecipesRepository
import me.sailex.ai.npc.llm.ILLMClient
import me.sailex.ai.npc.model.database.Conversation
import me.sailex.ai.npc.model.database.Recipe
import me.sailex.ai.npc.model.database.Resource
import me.sailex.ai.npc.util.LogUtil
import me.sailex.ai.npc.util.VectorUtil.cosineSimilarity

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
    private val llmClient: ILLMClient
) {
    private var executorService: ExecutorService = Executors.newFixedThreadPool(3)

    private val recipes = arrayListOf<Recipe>()
    private val conversations = arrayListOf<Conversation>()

    /**
     * Loads conversations recipes from db/mc into memory
     */
    fun loadResources(server: MinecraftServer, npcName: String) {
        runAsync {
            LogUtil.info("Loading resources into memory...")
            loadConversations(npcName)
            loadRecipes(server)
            executorService.shutdown()
        }
    }

    fun getRelevantRecipes(itemName: String): List<Recipe> {
        return getRelevantResources(itemName, recipes, 5).filterIsInstance<Recipe>()
    }

    fun getRelevantConversations(message: String): List<Conversation> {
        return getRelevantResources(message, conversations, 3).filterIsInstance<Conversation>()
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
        LogUtil.info("Saving resources into db...", true)
        val recipesFuture = runAsync {
            recipes.forEach { recipe -> recipesRepository.insert(recipe) }
        }
        val conversationFuture = runAsync {
            conversations.forEach { conversation -> conversationRepository.insert(conversation) }
        }
        CompletableFuture.allOf(recipesFuture, conversationFuture, functionFuture).get()
        executorService.shutdown()
    }

    private fun shutdownServiceNow() {
        if (!executorService.isTerminated) {
            executorService.shutdownNow()
            LogUtil.error("Initial loading of resources interrupted - Wait for termination", true)
            executorService.awaitTermination(500, TimeUnit.MILLISECONDS)
        }
    }

    private fun runAsync(task: () -> Unit): CompletableFuture<Void> {
        return CompletableFuture.runAsync(task , executorService).exceptionally {
            LogUtil.error("Error loading/saving resources into memory: ${it.stackTraceToString()}", true)
            return@exceptionally null
        }
    }

    private fun getRelevantResources(prompt: String, resources: List<Resource>, maxTopElements: Int): List<Resource> {
        val promptEmbedding = llmClient.generateEmbedding(listOf(prompt))
        return resources.map { resource -> Pair(resource, cosineSimilarity(promptEmbedding, resource.embedding)) }
            .sortedByDescending { it.second }
            .take(maxTopElements)
            .map { it.first }
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
        return idString.substring(idString.indexOf(".") + 1, idString.lastIndexOf("'"))
    }
}
