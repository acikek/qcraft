package com.acikek.qcraft.world.state.frequency;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LocationList<T extends Frequential> {

    public List<T> locations;
    public T removed = null;

    public LocationList() {
        locations = new ArrayList<>();
    }

    /**
     * @return The location at the specified block position, if present.
     */
    public Optional<T> get(BlockPos blockPos) {
        return locations.stream()
                .filter(loc -> loc.pos.asLong() == blockPos.asLong())
                .findFirst();
    }

    /**
     * Removes the block at the specified block position, if present.
     */
    public void remove(PersistentState data, BlockPos blockPos, boolean save) {
        get(blockPos).ifPresent(location -> remove(data, location, save));
    }

    /**
     * Removes the specified block location.
     *
     * @see LocationList#remove(PersistentState, BlockPos, boolean)
     */
    public void remove(PersistentState data, T location, boolean save) {
        if (locations.remove(location)) {
            if (save) {
                removed = location;
            }
            locations.remove(location);
            data.markDirty();
        }
    }

    public boolean has(BlockPos blockPos) {
        return get(blockPos).isPresent();
    }
}
