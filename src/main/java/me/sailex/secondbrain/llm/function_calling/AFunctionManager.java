package me.sailex.secondbrain.llm.function_calling;

import me.sailex.altoclef.AltoClefController;
import me.sailex.secondbrain.constant.Instructions;
import me.sailex.secondbrain.event.EventHandler;
import me.sailex.secondbrain.llm.roles.ChatRole;

import java.util.List;

public abstract class AFunctionManager<T> implements FunctionProvider<T> {

    protected static AltoClefController controller;
    protected static EventHandler eventHandler;
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

    protected static Runnable onTaskFinish() {
        return () -> {
            if (eventHandler.queueIsEmpty()) {
//                eventHandler.onEvent(ChatRole.SYSTEM, "");
            }
        };
    }
}
