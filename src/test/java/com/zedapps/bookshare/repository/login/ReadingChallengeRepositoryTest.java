package com.zedapps.bookshare.repository.login;

import com.zedapps.bookshare.entity.login.Login;
import com.zedapps.bookshare.entity.login.ReadingChallenge;
import com.zedapps.bookshare.util.TestUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author smzoha
 * @since 26/6/26
 **/
@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ReadingChallengeRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17-alpine");

    @Autowired
    private LoginRepository loginRepository;

    @Autowired
    private ReadingChallengeRepository readingChallengeRepository;

    private Login login;

    private int challengeYear;

    @BeforeAll
    void setUp() {
        login = TestUtils.getLogin("test@test.com", "test", true);
        loginRepository.saveAndFlush(login);

        challengeYear = LocalDate.now().getYear();

        ReadingChallenge readingChallenge = TestUtils.getReadingChallenge(login, challengeYear, 10);
        readingChallengeRepository.saveAndFlush(readingChallenge);
    }

    @Test
    void getReadingChallengeByLogin_EmailAndYear_existingYear_returnsChallenge() {
        Optional<ReadingChallenge> readingChallenge = readingChallengeRepository.getReadingChallengeByLogin_EmailAndYear(login.getEmail(), challengeYear);

        assertTrue(readingChallenge.isPresent());
        assertEquals(login, readingChallenge.get().getLogin());
        assertEquals(challengeYear, readingChallenge.get().getYear());
    }

    @Test
    void getReadingChallengeByLogin_EmailAndYear_missingYear_returnsEmptyOptional() {
        Optional<ReadingChallenge> readingChallenge = readingChallengeRepository.getReadingChallengeByLogin_EmailAndYear(login.getEmail(), 1990);

        assertFalse(readingChallenge.isPresent());
    }
}
