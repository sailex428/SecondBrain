package me.sailex.ai.npc.llm.function_calling

import io.github.sashirestela.openai.common.function.FunctionExecutor

interface IFunctionManager {

    var functionExecutor: FunctionExecutor

}