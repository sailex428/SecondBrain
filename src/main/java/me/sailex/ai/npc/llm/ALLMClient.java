package me.sailex.ai.npc.llm;

import java.util.List;

import lombok.Setter;
import me.sailex.ai.npc.llm.function_calling.IFunctionManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Setter
public abstract class ALLMClient implements ILLMClient {

	protected static final Logger LOGGER = LogManager.getLogger(ALLMClient.class);
	protected IFunctionManager functionManager;

	protected double[] convertEmbedding(List<List<Double>> embedding) {
		return embedding.stream()
				.flatMapToDouble(innerList -> innerList.stream().mapToDouble(Double::doubleValue))
				.toArray();
	}

	@Override
	public void checkServiceIsReachable() {
		// To be implemented by the child classes
	}
}
