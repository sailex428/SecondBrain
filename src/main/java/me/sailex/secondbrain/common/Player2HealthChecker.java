package me.sailex.secondbrain.common;

import me.sailex.secondbrain.llm.player2.Player2APIClient;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Player2HealthChecker {

    private final Player2APIClient player2APIClient;
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    public Player2HealthChecker() {
        this.player2APIClient = new Player2APIClient();
    }

    public void runSchedule() {
        executor.scheduleAtFixedRate(player2APIClient::getHealthStatus, 0,1, TimeUnit.MINUTES);
    }

    public void shutdown() {
        executor.shutdown();
    }

}
