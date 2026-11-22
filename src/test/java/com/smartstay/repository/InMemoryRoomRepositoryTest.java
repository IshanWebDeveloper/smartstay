package com.smartstay.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.smartstay.model.BedType;
import com.smartstay.model.Room;
import com.smartstay.model.RoomStatus;
import com.smartstay.model.RoomType;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Checks {@link InMemoryRoomRepository}.
 */
class InMemoryRoomRepositoryTest {

    private final InMemoryRoomRepository repository = new InMemoryRoomRepository();

    private static Room room(String id) {
        return new Room(id, RoomType.STANDARD, BedType.SINGLE, RoomStatus.AVAILABLE);
    }

    @Test
    @DisplayName("A saved room can be found by ID")
    void saveAndFind() {
        Room room = room("R101");
        repository.save(room);

        assertEquals(room, repository.findById("R101").orElseThrow());
        assertTrue(repository.existsById("R101"));
    }

    @Test
    @DisplayName("An unknown ID finds nothing")
    void unknownId() {
        assertTrue(repository.findById("R999").isEmpty());
        assertFalse(repository.existsById("R999"));
    }

    @Test
    @DisplayName("Saving the same ID replaces the room")
    void saveReplaces() {
        repository.save(room("R101"));
        Room replacement = new Room("R101", RoomType.SUITE, BedType.KING, RoomStatus.AVAILABLE);
        repository.save(replacement);

        assertEquals(1, repository.findAll().size());
        assertEquals(RoomType.SUITE, repository.findById("R101").orElseThrow().getType());
    }

    @Test
    @DisplayName("Deleting removes the room and reports whether it existed")
    void delete() {
        repository.save(room("R101"));

        assertTrue(repository.deleteById("R101"));
        assertFalse(repository.existsById("R101"));
        assertFalse(repository.deleteById("R101"));
    }

    @Test
    @DisplayName("The list returned cannot change the repository")
    void findAllIsUnmodifiable() {
        repository.save(room("R101"));
        List<Room> rooms = repository.findAll();
        Room other = room("R102");

        assertThrows(UnsupportedOperationException.class, () -> rooms.add(other));
    }
}
