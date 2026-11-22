package com.smartstay.algorithm;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * Iterative binary search over a list sorted by a key. O(log n) time.
 */
public final class BinarySearch {

    /** Returned by {@link #indexOf} when the key is not found. */
    public static final int NOT_FOUND = -1;

    private BinarySearch() {
    }

    /**
     * Finds the position of an item by its key.
     *
     * @param sorted   the items, sorted by {@code keyOf} in {@code keyOrder}
     * @param key      the key to look for
     * @param keyOf    extracts the key from an item
     * @param keyOrder the order of the keys
     * @param <T>      the item type
     * @param <K>      the key type
     * @return the index of a matching item, or {@link #NOT_FOUND}
     */
    public static <T, K> int indexOf(List<? extends T> sorted, K key,
                                     Function<? super T, ? extends K> keyOf,
                                     Comparator<? super K> keyOrder) {
        Objects.requireNonNull(sorted, "Sorted list is required.");
        Objects.requireNonNull(key, "Key is required.");
        Objects.requireNonNull(keyOf, "Key extractor is required.");
        Objects.requireNonNull(keyOrder, "Key order is required.");

        int low = 0;
        int high = sorted.size() - 1;
        while (low <= high) {
            // Written this way so low + high cannot overflow.
            int middle = low + (high - low) / 2;
            int comparison = keyOrder.compare(keyOf.apply(sorted.get(middle)), key);
            if (comparison == 0) {
                return middle;
            }
            if (comparison < 0) {
                low = middle + 1;
            } else {
                high = middle - 1;
            }
        }
        return NOT_FOUND;
    }

    /**
     * Finds an item by its key.
     *
     * @param sorted   the items, sorted by {@code keyOf} in {@code keyOrder}
     * @param key      the key to look for
     * @param keyOf    extracts the key from an item
     * @param keyOrder the order of the keys
     * @param <T>      the item type
     * @param <K>      the key type
     * @return the matching item, or empty
     */
    public static <T, K> Optional<T> find(List<? extends T> sorted, K key,
                                          Function<? super T, ? extends K> keyOf,
                                          Comparator<? super K> keyOrder) {
        int index = indexOf(sorted, key, keyOf, keyOrder);
        return index == NOT_FOUND ? Optional.empty() : Optional.of(sorted.get(index));
    }
}
