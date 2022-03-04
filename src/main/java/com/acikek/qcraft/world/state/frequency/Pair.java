package com.acikek.qcraft.world.state.frequency;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

public class Pair<T extends Frequential> {

    public Optional<T> left;
    public Optional<T> right = Optional.empty();

    public Pair(T first) {
        left = Optional.of(first);
    }

    public Pair(Optional<T> left, Optional<T> right) {
        this.left = left;
        this.right = right;
    }

    /**
     * Adds an item to the pair, assuming that the left value is already present.
     *
     * @return Whether the item was successfully added.
     */
    public boolean add(T second) {
        if (left.isPresent() && right.isEmpty()) {
            right = Optional.of(second);
            return true;
        }
        return false;
    }

    /**
     * Removes an item from the pair, either left or right.
     *
     * @return Whether the pair is empty and should be removed.
     */
    public boolean remove(T item) {
        if (left.isPresent() && left.get() == item) {
            if (right.isPresent()) {
                left = right;
                right = Optional.empty();
            }
            else {
                left = Optional.empty();
                return true;
            }
        }
        else if (right.isPresent() && right.get() == item) {
            right = Optional.empty();
        }
        return false;
    }

    /**
     * @return The other item in the pair.
     */
    public T getOther(T item) {
        if (left.isEmpty() || right.isEmpty()) {
            return null;
        }
        if (left.get() == item) {
            return right.get();
        }
        else if (right.get() == item) {
            return left.get();
        }
        return null;
    }

    /**
     * @return Both items in the pair.
     */
    public List<T> getBoth() {
        List<T> both = new ArrayList<>();
        both.add(left.orElse(null));
        both.add(right.orElse(null));
        return both;
    }

    public static <T extends Frequential, P extends Pair<T>> Codec<P> getCodec(Codec<T> codec, BiFunction<Optional<T>, Optional<T>, P> constructor) {
        return RecordCodecBuilder.create(instance ->
                instance.group(
                        codec.optionalFieldOf("left").forGetter(p -> p.left),
                        codec.optionalFieldOf("right").forGetter(p -> p.right)
                )
                        .apply(instance, constructor)
        );
    }
}
