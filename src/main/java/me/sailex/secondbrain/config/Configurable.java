package me.sailex.secondbrain.config;

public interface Configurable {
    /**
     * Returns the unique identifier for this configuration
     * @return The configuration identifier used in file naming
     */
    String getConfigName();
}
