package com.smartstay.repository;

import java.util.List;
import java.util.Optional;

/**
 * Stores and retrieves items by ID.
 *
 * @param <T>  the item type
 * @param <ID> the ID type
 */
public interface Repository<T, ID> {

    /**
     * Adds an item, or replaces the item with the same ID.
     *
     * @param item the item to store
     */
    void save(T item);

    /**
     * Finds an item by ID.
     *
     * @param id the ID
     * @return the item, or empty
     */
    Optional<T> findById(ID id);

    /** @return all items, as a list that cannot be changed */
    List<T> findAll();

    /**
     * Tells whether an item with this ID exists.
     *
     * @param id the ID
     * @return {@code true} if it exists
     */
    boolean existsById(ID id);

    /**
     * Removes an item.
     *
     * @param id the ID
     * @return {@code true} if an item was removed
     */
    boolean deleteById(ID id);
}
