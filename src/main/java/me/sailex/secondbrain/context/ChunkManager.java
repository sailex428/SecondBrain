package me.sailex.secondbrain.context;

import me.sailex.secondbrain.config.BaseConfig;
import me.sailex.secondbrain.model.context.BlockData;
import me.sailex.secondbrain.util.LogUtil;
import net.minecraft.block.BlockState;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.*;

import static me.sailex.secondbrain.util.MCDataUtil.getMiningLevel;
import static me.sailex.secondbrain.util.MCDataUtil.getToolNeeded;

public class ChunkManager {

    private final int verticalScanRange;
    private final int chunkRadius;

    private record ChunkData(Set<BlockData> blocks, long timestamp) {}

    private final ServerPlayerEntity npcEntity;
    private final Map<ChunkPos, ChunkData> chunks = new HashMap<>();
    private final long chunkExpiryTime;

    public ChunkManager(ServerPlayerEntity npcEntity, BaseConfig config) {
        this.npcEntity = npcEntity;
        this.verticalScanRange = config.getContextVerticalScanRange();
        this.chunkRadius = config.getContextChunkRadius();
        this.chunkExpiryTime = config.getChunkExpiryTime();
    }

    /**
     * Get block data of every block type nearest block to the npc
     * @return block data of all nearest blocks
     */
    public List<BlockData> getNearbyBlocks() {
        List<BlockData> allBlocks = getAllBlocks();
        Map<String, BlockData> nearestBlocks = new HashMap<>();

        for (BlockData block : allBlocks) {
            String blockType = block.type();
            if (!nearestBlocks.containsKey(blockType) ||
                    isCloser(block.position(), nearestBlocks.get(blockType).position())) {
                nearestBlocks.put(blockType, block);
            }
        }
        return new ArrayList<>(nearestBlocks.values());
    }

    public List<BlockData> getBlocksOfType(String type, int numberOfBlocks) {
        List<BlockData> blocksFound = new ArrayList<>();

        for (BlockData block : getAllBlocks()) {
            if (blocksFound.size() >= numberOfBlocks) {
                break;
            } else if (type.equals(block.type())) {
                blocksFound.add(block);
            }
        }
        if (blocksFound.size() < numberOfBlocks) {
            LogUtil.error("Only %s blocks found of %s (wanted: %s)".formatted(
                    blocksFound.size(), type, numberOfBlocks), true);
        }
        return blocksFound;
    }

    /**
     * Get all blocks in the chunks around the NPC
     * - Updates data of each chunk after expiry time
     * @return list of all block data
     */
    public List<BlockData> getAllBlocks() {
        World world = npcEntity.getWorld();
        ChunkPos centerChunk = npcEntity.getChunkPos();
        Set<ChunkPos> currentChunks = new HashSet<>();
        List<BlockData> allBlocks = new ArrayList<>();

        long currentTime = System.currentTimeMillis();

        for (int x = -chunkRadius; x <= chunkRadius; x++) {
            for (int z = -chunkRadius; z <= chunkRadius; z++) {
                ChunkPos pos = new ChunkPos(centerChunk.x + x, centerChunk.z + z);
                currentChunks.add(pos);

                boolean isLoaded = world.isChunkLoaded(pos.x, pos.z);
                ChunkData chunkData = chunks.get(pos);
                boolean isExpired = chunkData != null &&
                        (currentTime - chunkData.timestamp() > chunkExpiryTime);

                if (isLoaded && (chunkData == null || isExpired)) {
                    scanChunk(pos, currentTime);
                }

                if (isLoaded && chunks.containsKey(pos)) {
                    allBlocks.addAll(chunks.get(pos).blocks());
                }
            }
        }
        removeChunks(world, currentChunks);
        return allBlocks;
    }

    // Remove chunks that are out of range or unloaded
    private void removeChunks(World world, Set<ChunkPos> currentChunks) {
        Set<ChunkPos> toRemove = new HashSet<>();
        for (ChunkPos pos : chunks.keySet()) {
            if (!currentChunks.contains(pos) || !world.isChunkLoaded(pos.x, pos.z)) {
                toRemove.add(pos);
            }
        }

        if (!toRemove.isEmpty()) {
            toRemove.forEach(chunks::remove);
        }
    }

    private void scanChunk(ChunkPos chunk, long timestamp) {
        World world = npcEntity.getWorld();
        BlockPos.Mutable pos = new BlockPos.Mutable();
        int baseY = Math.max(0, npcEntity.getBlockPos().getY() - verticalScanRange);
        int maxY = Math.min(world.getHeight(), npcEntity.getBlockPos().getY() + verticalScanRange);

        Set<BlockData> blocks = new HashSet<>();

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = baseY; y < maxY; y++) {
                    pos.set(chunk.getStartX() + x, y, chunk.getStartZ() + z);

                    BlockState blockState = world.getBlockState(pos);
                    String blockType = blockState.getBlock().getName().getString().toLowerCase();

                    if (blockType.contains("air")) continue;

                    if (isAccessible(pos, world)) {
                        blocks.add(new BlockData(blockType, pos.toImmutable(),
                                getMiningLevel(blockState), getToolNeeded(blockState)));
                    }
                }
            }
        }
        chunks.put(chunk, new ChunkData(blocks, timestamp));
    }

    private boolean isAccessible(BlockPos pos, World world) {
        for (Direction dir : Direction.values()) {
            if (world.getBlockState(pos.offset(dir)).isAir()) {
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

    public void clearContext() {
        chunks.clear();
    }
}