package com.zedapps.bookshare.repository;

import com.zedapps.bookshare.entity.book.Genre;
import com.zedapps.bookshare.repository.book.GenreRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author smzoha
 * @since 26/4/26
 **/
@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class GenreRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    @Autowired
    private GenreRepository genreRepository;

    @Test
    void findGenreByName_returnGenre() {
        setupGenres();

        Optional<Genre> persistedGenre = genreRepository.findGenreByName("Test Genre");
        assertTrue(persistedGenre.isPresent());
        assertEquals("Test Genre", persistedGenre.get().getName());

        Optional<Genre> invalidGenre = genreRepository.findGenreByName("Invalid Genre");
        assertTrue(invalidGenre.isEmpty());
    }

    private void setupGenres() {
        Genre genre = new Genre();
        genre.setName("Test Genre");

        genreRepository.save(genre);
    }
}