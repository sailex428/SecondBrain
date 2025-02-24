package me.sailex.ai.npc.model.database

import io.github.sashirestela.openai.common.function.FunctionDef

data class OpenAiFunction(
    override val name: String,
    val function: FunctionDef,
    override val embedding: DoubleArray
): LLMFunction