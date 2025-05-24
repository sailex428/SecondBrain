package me.sailex.secondbrain.config;

import io.wispforest.endec.Endec;
import io.wispforest.endec.StructEndec;
import io.wispforest.endec.impl.StructEndecBuilder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class BaseConfig implements Configurable {
    private int llmTimeout = 10;
    private int contextChunkRadius = 4;
    private int contextVerticalScanRange = 8;
    private int chunkExpiryTime = 60;
    private boolean verbose = false;

    public int getLlmTimeout() {
        return llmTimeout;
    }

    public int getContextVerticalScanRange() {
        return contextVerticalScanRange;
    }

    public int getContextChunkRadius() {
        return contextChunkRadius;
    }

    public int getChunkExpiryTime() {
        return chunkExpiryTime;
    }

    public void setContextChunkRadius(int contextChunkRadius) {
        this.contextChunkRadius = contextChunkRadius;
    }

    public void setChunkExpiryTime(int chunkExpiryTime) {
        this.chunkExpiryTime = chunkExpiryTime;
    }

    public void setContextVerticalScanRange(int contextVerticalScanRange) {
        this.contextVerticalScanRange = contextVerticalScanRange;
    }

    public void setLlmTimeout(int llmTimeout) {
        this.llmTimeout = llmTimeout;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    @Override
    public String getConfigName() {
        return "base";
    }

    public static final StructEndec<BaseConfig> ENDEC = StructEndecBuilder.of(
            Endec.INT.fieldOf("llmTimeout", BaseConfig::getLlmTimeout),
            Endec.INT.fieldOf("contextChunkRadius", BaseConfig::getContextChunkRadius),
            Endec.INT.fieldOf("contextVerticalScanRange", BaseConfig::getContextVerticalScanRange),
            Endec.INT.fieldOf("chunkExpiryTime", BaseConfig::getChunkExpiryTime),
            Endec.BOOLEAN.fieldOf("verbose", BaseConfig::isVerbose),
            BaseConfig::new
    );

    @Override
    public String toString() {
        return "BaseConfig{" +
                "llmTimeout=" + llmTimeout +
                ",contextChunkRadius=" + contextChunkRadius +
                ",contextVerticalScanRange=" + contextVerticalScanRange +
                ",chunkExpiryTime=" + chunkExpiryTime +
                ",verbose=" + verbose +"}";
    }

    public static final String LLM_TIMEOUT_KEY = "LLM Service Timeout";
    public static final String CONTEXT_CHUNK_RADIUS_KEY = "Chunk Radius";
    public static final String CONTEXT_VERTICAL_RANGE_KEY = "Vertical Scan Range";
    public static final String CHUNK_EXPIRY_TIME_KEY = "Chunk Expiry Time";
    public static final String VERBOSE_KEY = "Debug Mode";
}
