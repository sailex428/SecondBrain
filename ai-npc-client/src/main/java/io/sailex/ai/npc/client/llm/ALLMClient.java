package io.sailex.ai.npc.client.llm;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class ALLMClient implements ILLMClient {

	protected static final Logger LOGGER = LogManager.getLogger(ALLMClient.class);
	protected final ExecutorService service;

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
}
