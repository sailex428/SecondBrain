package me.sailex.secondbrain.model.function_calling

import io.github.sashirestela.openai.common.function.FunctionDef

data class OpenAiFunction(
    override val name: String,
    val function: FunctionDef,
    override val embedding: DoubleArray
): LLMFunction