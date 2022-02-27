package com.acikek.qcraft.world.frequency;

import java.util.ArrayList;
import java.util.List;

public class Pair<T extends Frequential> {

    public T left;
    public T right = null;

    public Pair(T first) {
        left = first;
    }

    public Pair(T left, T right) {
        this.left = left;
        this.right = right;
    }

    /**
     * Adds an item to the pair, assuming that the left value is already present.
     *
     * @return Whether the item was successfully added.
     */
    public boolean add(T second) {
        if (left != null && right == null) {
            right = second;
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
        if (left == item) {
            if (right != null) {
                left = right;
                right = null;
            }
            else {
                left = null;
                return true;
            }
        }
        else if (right == item) {
            right = null;
        }
        return false;
    }

    /**
     * @return The other item in the pair.
     */
    public T getOther(T item) {
        if (left == item) {
            return right;
        }
        else if (right == item) {
            return left;
        }
        return null;
    }

    /**
     * @return Both items in the pair.
     */
    public List<T> getBoth() {
        List<T> both = new ArrayList<>();
        both.add(left);
        both.add(right);
        return both;
    }
}
