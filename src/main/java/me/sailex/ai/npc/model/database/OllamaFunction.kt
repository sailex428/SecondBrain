package me.sailex.ai.npc.model.database

import io.github.ollama4j.tools.Tools

data class OllamaFunction(
    override val name: String,
    val function: Tools.ToolSpecification,
    override val embedding: DoubleArray
): LLMFunction