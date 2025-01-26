package me.sailex.ai.npc.llm;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import me.sailex.ai.npc.llm.function_calling.IFunctionManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class ALLMClient implements ILLMClient {

	protected static final Logger LOGGER = LogManager.getLogger(ALLMClient.class);
	protected final ExecutorService service;
	protected IFunctionManager functionManager;

	protected ALLMClient() {
		this.service = Executors.newFixedThreadPool(3);
	}

	protected double[] convertEmbedding(List<List<Double>> embedding) {
		return embedding.stream()
				.flatMapToDouble(innerList -> innerList.stream().mapToDouble(Double::doubleValue))
				.toArray();
	}

	@Override
	public void stopService() {
		service.shutdown();
	}

	@Override
	public void checkServiceIsReachable() {
		// To be implemented by the child classes
	}

	@Override
	public void setFunctionManager(IFunctionManager functionManager) {
		this.functionManager = functionManager;
	}
}
