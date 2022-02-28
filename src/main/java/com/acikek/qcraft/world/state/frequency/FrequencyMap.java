package com.acikek.qcraft.world.state.frequency;

import com.acikek.qcraft.QCraft;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class FrequencyMap<T extends Frequential, P extends Pair<T>> {

    public Map<UUID, P> frequencies = new HashMap<>();

    public void filter(List<T> list) {
        int size = frequencies.size();
        for (Map.Entry<UUID, P> frequency : frequencies.entrySet()) {
            for (T item : frequency.getValue().getBoth()) {
                if (item != null && !list.contains(item  )) {
                    frequencies.remove(frequency.getKey());
                    break;
                }
            }
        }
        if (frequencies.size() < size) {
            QCraft.LOGGER.error("Removed " + (size - frequencies.size()) + " invalid frequencies");
        }
    }

    public void fill(List<T> list, Function<T, ? extends P> ctor) {
        for (T item : list) {
            item.frequency.ifPresent(frequency -> {
                if (!frequencies.containsKey(frequency)) {
                    frequencies.put(frequency, ctor.apply(item));
                }
                else if (!frequencies.get(frequency).add(item)) {
                    QCraft.LOGGER.error("Failed to add " + item + " to frequency '" + frequency + "'");
                }
            });
        }
    }

    public void ifPresent(Frequential item, Consumer<P> consumer) {
        item.frequency.ifPresent(frequency -> {
            if (frequencies.containsKey(frequency)) {
                consumer.accept(frequencies.get(frequency));
            }
            else {
                QCraft.LOGGER.error(item + " has an unmatched frequency '" + frequency + "'");
                item.frequency = Optional.empty();
            }
        });
    }

    public void add(UUID uuid, T item, Function<T, ? extends P> ctor) {
        if (frequencies.containsKey(uuid)) {
            frequencies.get(uuid).add(item);
        }
        else {
            frequencies.put(uuid, ctor.apply(item));
        }
    }

    public void remove(T item) {
        ifPresent(item, pair -> {
            if (pair.remove(item) && item.frequency.isPresent()) {
                frequencies.remove(item.frequency.get());
            }
        });
    }
}
