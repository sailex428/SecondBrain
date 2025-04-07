package me.sailex.secondbrain.model.function_calling

import io.github.ollama4j.tools.Tools

data class OllamaFunction(
    override val name: String,
    val function: Tools.ToolSpecification,
    override val embedding: DoubleArray
): LLMFunction