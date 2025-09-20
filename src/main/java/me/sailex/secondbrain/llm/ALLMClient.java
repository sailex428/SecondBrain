package me.sailex.secondbrain.llm;

import lombok.Setter;
import me.sailex.secondbrain.llm.function_calling.FunctionProvider;

@Setter
public abstract class ALLMClient<T> implements FunctionCallable {

    protected FunctionProvider<T> functionManager;

    protected ALLMClient(FunctionProvider<T> functionManager) {
        this.functionManager = functionManager;
    }

	@Override
	public void checkServiceIsReachable() {
		// To be implemented by the child classes
	}

	@Override
	public void stopService() {}

}
