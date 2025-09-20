package me.sailex.secondbrain.llm.function_calling;

import me.sailex.altoclef.AltoClefController;

import java.util.List;

public abstract class AFunctionManager<T> implements FunctionProvider<T> {

    protected static AltoClefController controller;
    protected List<T> functions;

    protected AFunctionManager(AltoClefController controller) {
        AFunctionManager.controller = controller;
        this.functions = createFunctions();
    }

    @Override
    public List<T> getFunctions() {
        return functions;
    }

    protected abstract List<T> createFunctions();

}
