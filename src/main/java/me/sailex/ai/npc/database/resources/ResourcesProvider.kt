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

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ResourcesProvider(
    private val conversationRepository: ConversationRepository,
    private val recipesRepository: RecipesRepository,
    private val llmClient: ILLMClient
) {
    private var executorService: ExecutorService = Executors.newSingleThreadExecutor()

    private val recipes = arrayListOf<Recipe>()
    private val conversations = arrayListOf<Conversation>()

    /**
     * Loads conversations from db and recipes from mc into memory
     */
    fun loadResources(server: MinecraftServer, npcName: String) {
        executorService.submit {
            LogUtil.info("Loading resources into memory...", true)
            loadConversations(npcName)
            loadRecipes(server)
        }
        executorService.shutdown()
    }

    fun getRelevantRecipes(itemName: String): List<Recipe> {
        return getRelevantResources(itemName, recipes, 5).filterIsInstance<Recipe>()
    }

    fun getRelevantConversations(message: String): List<Conversation> {
        return getRelevantResources(message, conversations, 2).filterIsInstance<Conversation>()
    }

    fun addConversation(npcName: String, timestamp: Timestamp, message: String) {
        this.conversations.add(Conversation(
            npcName, message,
            llmClient.generateEmbedding(listOf(message)),
            timestamp))
    }

    fun saveResources() {
        executorService = Executors.newSingleThreadExecutor()
        executorService.submit {
            LogUtil.info("Saving resources into db...", true)
            recipes.forEach { recipesRepository::insert }
            conversations.forEach { conversationRepository::insert }
        }
        executorService.shutdown()
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

        //if no new recipes are added by other mods, load from the db (avoid re-vectorize the names)
        if (recipesRepository.selectCount() == recipeEntries.size) {
            this.recipes.addAll(recipesRepository.selectAll().filterIsInstance<Recipe>())
            return
        }

        recipeEntries.forEach { entry ->
            this.recipes.add(buildRecipe(entry))
        }
    }

    private fun buildRecipe(entry: RecipeEntry<*>): Recipe {
        val recipeName = entry.id.path
        val recipeValue = entry.value
        val ingredients = recipeValue.ingredients

        return Recipe(
            recipeValue.type.toString(),
            recipeName,
            llmClient.generateEmbedding(listOf(recipeName)),
            recipeValue.createIcon().name.string,
            getItemNeeded(ingredients)
        )
    }

    private fun getItemNeeded(ingredients: DefaultedList<Ingredient>): String {
        return ingredients.filter { it.matchingStacks.isNullOrEmpty() }
            .joinToString(",") { "${getItemId(it)}=${it.matchingStacks.size}" }
    }

    private fun getItemId(ingredient: Ingredient): String {
        val idString = ingredient.matchingStacks[0].name.toString()
        return idString.substring(idString.indexOf(".") + 1, idString.lastIndexOf("'"))
    }
}
