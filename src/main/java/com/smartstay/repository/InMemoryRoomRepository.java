package com.smartstay.repository;

import com.smartstay.model.Room;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Rooms in a {@link HashMap} keyed by room ID, for O(1) lookup.
 * The order of {@link #findAll()} is not defined; callers sort it.
 */
public final class InMemoryRoomRepository implements Repository<Room, String> {

    private final Map<String, Room> roomsById = new HashMap<>();

    @Override
    public void save(Room room) {
        Objects.requireNonNull(room, "Room is required.");
        roomsById.put(room.getRoomId(), room);
    }

    @Override
    public Optional<Room> findById(String roomId) {
        return Optional.ofNullable(roomsById.get(roomId));
    }

    @Override
    public List<Room> findAll() {
        return List.copyOf(roomsById.values());
    }

    @Override
    public boolean existsById(String roomId) {
        return roomsById.containsKey(roomId);
    }

    @Override
    public boolean deleteById(String roomId) {
        return roomsById.remove(roomId) != null;
    }
}
