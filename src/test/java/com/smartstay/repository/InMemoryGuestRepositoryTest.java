package com.smartstay.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.smartstay.model.Guest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Checks {@link InMemoryGuestRepository}.
 */
class InMemoryGuestRepositoryTest {

    private final InMemoryGuestRepository repository = new InMemoryGuestRepository();

    private static Guest nimal() {
        return new Guest("199012345678", "Nimal Perera", "0771234567", "nimal@example.com");
    }

    @Test
    @DisplayName("A saved guest can be found by NIC")
    void saveAndFind() {
        repository.save(nimal());

        assertEquals("Nimal Perera", repository.findById("199012345678").orElseThrow().getFullName());
        assertTrue(repository.existsById("199012345678"));
    }

    @Test
    @DisplayName("An unregistered NIC finds nothing")
    void unknownNic() {
        assertTrue(repository.findById("188888888V").isEmpty());
        assertFalse(repository.existsById("188888888V"));
    }

    @Test
    @DisplayName("All guests are listed and can be removed")
    void listAndDelete() {
        repository.save(nimal());
        repository.save(new Guest("P4826153", "Sarah Johnson", "0700000000", "sarah@example.com"));
        assertEquals(2, repository.findAll().size());

        assertTrue(repository.deleteById("P4826153"));
        assertEquals(1, repository.findAll().size());
        assertFalse(repository.deleteById("P4826153"));
    }
}
