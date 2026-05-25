package com.zedapps.bookshare.repository.login;

import com.zedapps.bookshare.entity.login.Login;
import com.zedapps.bookshare.entity.login.Shelf;
import com.zedapps.bookshare.util.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author smzoha
 * @since 28/4/26
 **/
@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class ShelfRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17-alpine");

    @Autowired
    private ShelfRepository shelfRepository;

    @Autowired
    private LoginRepository loginRepository;

    private List<Shelf> shelfList;

    @BeforeEach
    void setup() {
        Login login = TestUtils.getLogin("user@test.com", "user", true);

        loginRepository.saveAndFlush(login);

        Shelf defaultShelf = TestUtils.getShelf(login, "Default Shelf", true);
        Shelf shelf = TestUtils.getShelf(login, "Test Shelf", false);

        shelfList = shelfRepository.saveAllAndFlush(List.of(defaultShelf, shelf));
    }

    @Test
    void getShelvesForCollection_returnShelfList() {
        List<Shelf> shelves = shelfRepository.getShelvesForCollection("user@test.com");

        assertFalse(shelves.isEmpty());

        assertTrue(shelves.getFirst().isDefaultShelf());
        assertFalse(shelves.getLast().isDefaultShelf());

        assertEquals(shelfList.getFirst(), shelves.getFirst());
        assertEquals(shelfList.getLast(), shelves.getLast());

        List<Shelf> noShelves = shelfRepository.getShelvesForCollection("invalid@test.com");
        assertTrue(noShelves.isEmpty());
    }

    @Test
    void findById_returnShelf() {
        Shelf originalShelf = shelfList.getFirst();
        Optional<Shelf> shelfOptional = shelfRepository.findById(originalShelf.getId());

        assertTrue(shelfOptional.isPresent());
        assertEquals(originalShelf, shelfOptional.get());

        Optional<Shelf> invalidShelfOptional = shelfRepository.findById(1000L);
        assertTrue(invalidShelfOptional.isEmpty());
    }

    @Test
    void existsShelfByNameAndUserEmail_returnBoolean() {
        assertTrue(shelfRepository.existsShelfByNameAndUser_Email("Default Shelf", "user@test.com"));
        assertTrue(shelfRepository.existsShelfByNameAndUser_Email("Test Shelf", "user@test.com"));

        assertFalse(shelfRepository.existsShelfByNameAndUser_Email("Default Shelf", "invalid@test.com"));
        assertFalse(shelfRepository.existsShelfByNameAndUser_Email("Invalid Shelf", "user@test.com"));
    }
}
