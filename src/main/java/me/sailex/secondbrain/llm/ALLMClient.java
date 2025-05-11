package me.sailex.secondbrain.llm;

import java.util.List;

import lombok.Setter;

@Setter
public abstract class ALLMClient<T> implements FunctionCallable<T> {

	protected double[] convertEmbedding(List<List<Double>> embedding) {
		return embedding.stream()
				.flatMapToDouble(innerList -> innerList.stream().mapToDouble(Double::doubleValue))
				.toArray();
	}

	@Override
	public void checkServiceIsReachable() {
		// To be implemented by the child classes
	}

	@Override
	public void stopService() {}

}
