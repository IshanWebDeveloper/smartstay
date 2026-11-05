package com.smartstay.repository;

import com.smartstay.model.Guest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Guests in a {@link HashMap} keyed by NIC or passport number, for an O(1) duplicate check.
 */
public final class InMemoryGuestRepository implements Repository<Guest, String> {

    private final Map<String, Guest> guestsById = new HashMap<>();

    @Override
    public void save(Guest guest) {
        Objects.requireNonNull(guest, "Guest is required.");
        guestsById.put(guest.getNicOrPassport(), guest);
    }

    @Override
    public Optional<Guest> findById(String nicOrPassport) {
        return Optional.ofNullable(guestsById.get(nicOrPassport));
    }

    @Override
    public List<Guest> findAll() {
        return List.copyOf(guestsById.values());
    }

    @Override
    public boolean existsById(String nicOrPassport) {
        return guestsById.containsKey(nicOrPassport);
    }

    @Override
    public boolean deleteById(String nicOrPassport) {
        return guestsById.remove(nicOrPassport) != null;
    }
}
