package com.smartstay.algorithm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Checks {@link MergeSort}.
 */
class MergeSortTest {

    private static final Comparator<Integer> ASCENDING = Comparator.naturalOrder();

    /** An item whose label shows its original position, to check stability. */
    private record Item(int key, String label) {
    }

    @Test
    @DisplayName("Empty list stays empty")
    void emptyList() {
        assertTrue(MergeSort.sort(List.<Integer>of(), ASCENDING).isEmpty());
    }

    @Test
    @DisplayName("Single item is returned unchanged")
    void singleItem() {
        assertEquals(List.of(7), MergeSort.sort(List.of(7), ASCENDING));
    }

    @Test
    @DisplayName("Already sorted list stays sorted")
    void alreadySorted() {
        assertEquals(List.of(1, 2, 3, 4, 5), MergeSort.sort(List.of(1, 2, 3, 4, 5), ASCENDING));
    }

    @Test
    @DisplayName("Reversed list is sorted")
    void reversed() {
        assertEquals(List.of(1, 2, 3, 4, 5), MergeSort.sort(List.of(5, 4, 3, 2, 1), ASCENDING));
    }

    @Test
    @DisplayName("Equal items keep their original order")
    void stableWithDuplicates() {
        List<Item> items = List.of(
                new Item(2, "a"), new Item(1, "b"), new Item(2, "c"),
                new Item(1, "d"), new Item(2, "e"));

        List<Item> sorted = MergeSort.sort(items, Comparator.comparingInt(Item::key));

        assertEquals(List.of("b", "d", "a", "c", "e"), sorted.stream().map(Item::label).toList());
    }

    @Test
    @DisplayName("Sorts by any comparator, for example descending")
    void customComparator() {
        assertEquals(List.of("R402", "R301", "R101"),
                MergeSort.sort(List.of("R301", "R101", "R402"), Comparator.<String>reverseOrder()));
    }

    @Test
    @DisplayName("The input list is not changed")
    void inputUnchanged() {
        List<Integer> input = new ArrayList<>(List.of(3, 1, 2));
        MergeSort.sort(input, ASCENDING);
        assertEquals(List.of(3, 1, 2), input);
    }

    @Test
    @DisplayName("Random lists match the library sort")
    void matchesLibrarySortOnRandomInput() {
        Random random = new Random(42);
        for (int size = 0; size < 50; size++) {
            List<Integer> input = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                input.add(random.nextInt(20));
            }
            List<Integer> expected = new ArrayList<>(input);
            expected.sort(ASCENDING);

            assertEquals(expected, MergeSort.sort(input, ASCENDING), "size " + size);
        }
    }
}
