package com.smartstay.algorithm;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Stable, top-down merge sort. O(n log n) time, O(n) extra space.
 */
public final class MergeSort {

    private MergeSort() {
    }

    /**
     * Returns a sorted copy of a list. The input is not changed.
     *
     * @param items      the items to sort
     * @param comparator the order to sort by
     * @param <T>        the item type
     * @return a new sorted list
     */
    public static <T> List<T> sort(List<? extends T> items, Comparator<? super T> comparator) {
        Objects.requireNonNull(items, "Items are required.");
        Objects.requireNonNull(comparator, "Comparator is required.");
        List<T> work = new ArrayList<>(items);
        List<T> buffer = new ArrayList<>(items);
        sortRange(work, buffer, 0, work.size(), comparator);
        return work;
    }

    // Sorts work[from, to) using buffer as scratch space.
    private static <T> void sortRange(List<T> work, List<T> buffer, int from, int to,
                                      Comparator<? super T> comparator) {
        if (to - from < 2) {
            return;
        }
        int middle = from + (to - from) / 2;
        sortRange(work, buffer, from, middle, comparator);
        sortRange(work, buffer, middle, to, comparator);
        merge(work, buffer, from, middle, to, comparator);
    }

    // Merges the sorted halves work[from, middle) and work[middle, to).
    private static <T> void merge(List<T> work, List<T> buffer, int from, int middle, int to,
                                  Comparator<? super T> comparator) {
        for (int i = from; i < to; i++) {
            buffer.set(i, work.get(i));
        }
        int left = from;
        int right = middle;
        int out = from;
        while (left < middle && right < to) {
            // "<= 0" takes the left item on ties, which keeps equal items in their original order.
            if (comparator.compare(buffer.get(left), buffer.get(right)) <= 0) {
                work.set(out++, buffer.get(left++));
            } else {
                work.set(out++, buffer.get(right++));
            }
        }
        while (left < middle) {
            work.set(out++, buffer.get(left++));
        }
        // Any items left on the right are already in place.
    }
}
