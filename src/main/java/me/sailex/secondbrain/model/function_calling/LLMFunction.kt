package me.sailex.secondbrain.model.function_calling

import me.sailex.secondbrain.model.database.Resource

interface LLMFunction : Resource {
    val name: String
}