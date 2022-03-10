package com.acikek.qcraft.world.state;

import com.acikek.qcraft.QCraft;
import com.acikek.qcraft.world.state.frequency.FrequencyMap;
import com.acikek.qcraft.world.state.frequency.Frequential;
import com.acikek.qcraft.world.state.frequency.LocationList;
import com.acikek.qcraft.world.state.frequency.Pair;
import net.minecraft.world.PersistentState;

import java.util.List;
import java.util.function.Function;

public abstract class LocationState<T extends Frequential, P extends Pair<T>> extends PersistentState {

    public LocationList<T> locations = new LocationList<>();
    public FrequencyMap<T, P> frequencies = new FrequencyMap<>();

    public LocationState() {
    }

    public void reset() {
        locations.list.clear();
        frequencies.map.clear();
    }

    public void fill(List<T> locations, Function<T, P> f) {
        this.locations.list.addAll(locations);
        QCraft.LOGGER.info("Loaded " + this.locations.list.size() + " qBlocks");
        frequencies.fill(locations, f);
        //QCraft.LOGGER.info("Loaded " + frequencies.map.size() + " frequencies");
    }

    public void remove(T location, boolean save) {
        locations.remove(location, save);
        frequencies.remove(location);
        markDirty();
    }

    public static class Keys {

        public String id;
        public String data;

        public Keys(String item) {
            id = QCraft.uid(item + "_data");
            data = QCraft.uid(item + "_locations");
        }
    }
}
