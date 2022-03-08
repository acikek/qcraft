package com.acikek.qcraft.world.state.frequency;

import com.acikek.qcraft.QCraft;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class FrequencyMap<T extends Frequential, P extends Pair<T>> {

    public Map<UUID, P> map = new HashMap<>();

    public void filter(LocationList<T> list) {
        int size = map.size();
        for (Map.Entry<UUID, P> frequency : map.entrySet()) {
            for (T item : frequency.getValue().getBoth()) {
                if (item != null && !list.list.contains(item)) {
                    map.remove(frequency.getKey());
                    break;
                }
            }
        }
        if (map.size() < size) {
            QCraft.LOGGER.error("Removed " + (size - map.size()) + " invalid frequencies");
        }
    }

    public void fill(List<T> list, Function<T, P> ctor) {
        for (T item : list) {
            item.frequency.ifPresent(frequency -> {
                if (!map.containsKey(frequency)) {
                    map.put(frequency, ctor.apply(item));
                }
                else if (!map.get(frequency).add(item)) {
                    QCraft.LOGGER.error("Failed to add " + item + " to frequency '" + frequency + "'");
                }
            });
        }
    }

    public void ifPresent(Frequential item, Consumer<P> consumer) {
        item.frequency.ifPresent(frequency -> {
            if (map.containsKey(frequency)) {
                consumer.accept(map.get(frequency));
            }
            else {
                QCraft.LOGGER.error(item + " has an unmatched frequency '" + frequency + "'");
                item.frequency = Optional.empty();
            }
        });
    }

    public void add(UUID uuid, T item, Function<T, ? extends P> ctor) {
        if (map.containsKey(uuid)) {
            map.get(uuid).add(item);
        }
        else {
            map.put(uuid, ctor.apply(item));
        }
    }

    public void remove(T item) {
        ifPresent(item, pair -> {
            if (pair.remove(item) && item.frequency.isPresent()) {
                map.remove(item.frequency.get());
            }
        });
    }
}
