package me.sailex.secondbrain.llm;

import java.util.List;

import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Setter
public abstract class ALLMClient<T> implements FunctionCallable<T> {

	protected double[] convertEmbedding(List<List<Double>> embedding) {
		return embedding.stream()
				.flatMapToDouble(innerList -> innerList.stream().mapToDouble(Double::doubleValue))
				.toArray();
	}

	@Override
	public void checkServiceIsReachable(String url) {
		// To be implemented by the child classes
	}

	@Override
	public void stopService() {}

}
