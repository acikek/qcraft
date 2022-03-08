package com.acikek.qcraft.world.state.frequency;

import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LocationList<T extends Frequential> {

    public List<T> list;
    public T removed = null;

    public LocationList() {
        list = new ArrayList<>();
    }

    /**
     * @return The location at the specified block position, if present.
     */
    public Optional<T> get(BlockPos blockPos) {
        return list.stream()
                .filter(loc -> loc.pos.asLong() == blockPos.asLong())
                .findFirst();
    }

    /**
     * Removes the specified block location.
     */
    public void remove(T location, boolean save) {
        if (list.remove(location)) {
            if (save) {
                removed = location;
            }
            list.remove(location);
        }
    }

    public boolean has(BlockPos blockPos) {
        return get(blockPos).isPresent();
    }
}
