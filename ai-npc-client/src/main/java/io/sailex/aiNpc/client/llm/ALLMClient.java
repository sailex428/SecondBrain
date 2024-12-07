package io.sailex.aiNpc.client.llm;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class ALLMClient implements ILLMClient {

    protected static final Logger LOGGER = LogManager.getLogger(ALLMClient.class);
    protected final ExecutorService service;

    public ALLMClient() {
        this.service = Executors.newFixedThreadPool(3);
    }

    @Override
    public abstract String generateResponse(String userPrompt, String systemPrompt);

    @Override
    public abstract Float[] generateEmbedding(List<String> prompt);

    protected Float[] convertEmbedding(List<List<Double>> embedding) {
        return embedding.stream().flatMap(Collection::stream)
                .map(Double::floatValue)
                .toArray(Float[]::new);
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
