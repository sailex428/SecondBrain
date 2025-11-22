package me.sailex.secondbrain.context;

import lombok.Getter;
import me.sailex.altoclef.multiversion.EntityVer;
import me.sailex.secondbrain.config.BaseConfig;
import me.sailex.secondbrain.model.context.BlockData;
import me.sailex.secondbrain.util.LogUtil;
import net.minecraft.block.BlockState;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;

import java.util.*;
import java.util.concurrent.*;

import static me.sailex.secondbrain.util.MCDataUtil.getMiningLevel;
import static me.sailex.secondbrain.util.MCDataUtil.getToolNeeded;

public class ChunkManager {

    private final int verticalScanRange;
    private final int chunkRadius;

    private final ServerPlayerEntity npcEntity;
    private final ScheduledExecutorService threadPool;
    private final List<BlockData> currentLoadedBlocks;

    @Getter
    private final List<BlockData> nearbyBlocks = new ArrayList<>();


    public ChunkManager(ServerPlayerEntity npcEntity, BaseConfig config) {
        this.npcEntity = npcEntity;
        this.verticalScanRange = config.getContextVerticalScanRange();
        this.chunkRadius = config.getContextChunkRadius();
        this.currentLoadedBlocks = new ArrayList<>();
        this.threadPool = Executors.newSingleThreadScheduledExecutor();
        scheduleRefreshBlocks(config.getChunkExpiryTime());
    }

    private void scheduleRefreshBlocks(int chunkExpiryTime) {
        threadPool.scheduleAtFixedRate(() -> {
            synchronized (this) {
                updateAllBlocks();
                updateNearbyBlocks();
            }
        }, 0, chunkExpiryTime, TimeUnit.SECONDS);
    }

    public List<BlockData> getBlocksOfType(String type, int numberOfBlocks) {
        List<BlockData> blocksFound = new ArrayList<>();

        for (BlockData block : currentLoadedBlocks) {
            if (blocksFound.size() >= numberOfBlocks) {
                break;
            } else if (type.equals(block.type())) {
                blocksFound.add(block);
            }
        }
        if (blocksFound.size() < numberOfBlocks) {
            LogUtil.error("Only %s blocks found of %s (wanted: %s)".formatted(
                    blocksFound.size(), type, numberOfBlocks));
        }
        return blocksFound;
    }

    /**
     * Updates block data of every block type nearest block to the npc
     */
    private void updateNearbyBlocks() {
        Map<String, BlockData> nearestBlocks = new HashMap<>();

        for (BlockData block : currentLoadedBlocks) {
            String blockType = block.type();
            if (!nearestBlocks.containsKey(blockType) ||
                    isCloser(block.position(), nearestBlocks.get(blockType).position())) {
                nearestBlocks.put(blockType, block);
            }
        }
        this.nearbyBlocks.addAll(nearestBlocks.values());
    }

    /**
     * Updates all blocks in the chunks around the NPC
     */
    private void updateAllBlocks() {
        currentLoadedBlocks.clear();
        World world = EntityVer.getWorld(npcEntity);
        ChunkPos centerChunk = npcEntity.getChunkPos();

        for (int x = -chunkRadius; x <= chunkRadius; x++) {
            for (int z = -chunkRadius; z <= chunkRadius; z++) {
                ChunkPos pos = new ChunkPos(centerChunk.x + x, centerChunk.z + z);

                boolean isLoaded = world.isChunkLoaded(pos.x, pos.z);

                if (isLoaded) {
                    currentLoadedBlocks.addAll(scanChunk(pos));
                }
            }
        }
    }

    private List<BlockData> scanChunk(ChunkPos chunk) {
        World world = EntityVer.getWorld(npcEntity);
        BlockPos.Mutable pos = new BlockPos.Mutable();
        int baseY = Math.max(0, npcEntity.getBlockPos().getY() - verticalScanRange);
        int maxY = Math.min(world.getHeight(), npcEntity.getBlockPos().getY() + verticalScanRange);

        List<BlockData> blocks = new ArrayList<>();

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = baseY; y < maxY; y++) {
                    pos.set(chunk.getStartX() + x, y, chunk.getStartZ() + z);
                    WorldChunk currentChunk = world.getWorldChunk(pos);

                    BlockState blockState = currentChunk.getBlockState(pos);
                    String blockType = blockState.getBlock()
                            .getName().getString()
                            .toLowerCase().replace(" ", "_");

                    if (blockType.contains("air")) continue;

                    if (isAccessible(pos, currentChunk)) {
                        blocks.add(new BlockData(blockType, pos.toImmutable(),
                                getMiningLevel(blockState), getToolNeeded(blockState)));
                    }
                }
            }
        }
        return blocks;
    }

    private boolean isAccessible(BlockPos pos, WorldChunk chunk) {
        for (Direction dir : Direction.values()) {
            if (chunk.getBlockState(pos.offset(dir)).isAir()) {
                return true;
            }
        }
        return false;
    }

    private boolean isCloser(BlockPos pos1, BlockPos pos2) {
        double dist1 = npcEntity.getBlockPos().getSquaredDistance(pos1);
        double dist2 = npcEntity.getBlockPos().getSquaredDistance(pos2);
        return dist1 < dist2;
    }

    public void stopService() {
        this.threadPool.shutdownNow();
    }
}