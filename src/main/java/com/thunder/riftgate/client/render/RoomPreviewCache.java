package com.thunder.riftgate.client.render;

import net.minecraft.world.level.block.state.BlockState;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class RoomPreviewCache {
    private static final Map<UUID, RoomPreviewSnapshot> SNAPSHOTS = new ConcurrentHashMap<>();

    private RoomPreviewCache() {}

    public static void update(UUID playerId, List<BlockInstance> blocks) {
        SNAPSHOTS.put(playerId, new RoomPreviewSnapshot(List.copyOf(blocks)));
    }

    public static RoomPreviewSnapshot get(UUID playerId) {
        return SNAPSHOTS.get(playerId);
    }

    public record BlockInstance(int dx, int dy, int dz, BlockState state) {}

    public record RoomPreviewSnapshot(List<BlockInstance> blocks) {
        public static final RoomPreviewSnapshot EMPTY = new RoomPreviewSnapshot(Collections.emptyList());
    }
}
