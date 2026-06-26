package com.zedapps.bookshare.repository.login;

import com.zedapps.bookshare.entity.book.Author;
import com.zedapps.bookshare.entity.book.Book;
import com.zedapps.bookshare.entity.login.Login;
import com.zedapps.bookshare.entity.login.ReadingProgress;
import com.zedapps.bookshare.enums.Status;
import com.zedapps.bookshare.repository.book.AuthorRepository;
import com.zedapps.bookshare.repository.book.BookRepository;
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
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author smzoha
 * @since 26/6/26
 **/
@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ReadingProgressRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17-alpine");

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private LoginRepository loginRepository;

    @Autowired
    private ReadingProgressRepository readingProgressRepository;

    private Login login;
    private Login otherLogin;

    private Book book;

    @BeforeAll
    void setUp() {
        login = TestUtils.getLogin("test@test.com", "test", true);
        otherLogin = TestUtils.getLogin("other@test.com", "other", true);

        loginRepository.saveAllAndFlush(List.of(login, otherLogin));

        Author author = TestUtils.getAuthor("Test", "Author");
        authorRepository.saveAndFlush(author);

        book = TestUtils.getBook("Test Book", "9780451524935", author, Status.ACTIVE);
        bookRepository.saveAndFlush(book);

        ReadingProgress loginProgressCurrentYear = TestUtils.getReadingProgress(book, login, 10L, LocalDate.now(),
                LocalDate.now(), true);

        ReadingProgress loginProgressPrevYear = TestUtils.getReadingProgress(book, login, 10L, LocalDate.now(),
                LocalDate.now().minusYears(1), true);

        ReadingProgress loginProgressIncomplete = TestUtils.getReadingProgress(book, login, 10L, LocalDate.now(),
                LocalDate.now(), false);

        ReadingProgress otherLoginProgress = TestUtils.getReadingProgress(book, otherLogin, 10L, LocalDate.now(),
                LocalDate.now(), true);

        readingProgressRepository.saveAllAndFlush(List.of(
                loginProgressCurrentYear,
                loginProgressPrevYear,
                loginProgressIncomplete,
                otherLoginProgress
        ));
    }

    @Test
    void findReadingProgressesByUser_EmailAndEndDateYear_returnsOnlyCompletedForUserAndYear() {
        int currentYear = LocalDate.now().getYear();
        List<ReadingProgress> progresses = readingProgressRepository.findReadingProgressesByUser_EmailAndEndDateYear(
                login.getEmail(), currentYear);

        assertFalse(progresses.isEmpty());
        assertEquals(1, progresses.size());
        assertEquals(login, progresses.getFirst().getUser());
        assertEquals(currentYear, progresses.getFirst().getEndDate().getYear());
        assertEquals(book, progresses.getFirst().getBook());

        assertTrue(progresses.stream().noneMatch(rp -> Objects.equals(rp.getUser(), otherLogin)));
    }

    @Test
    void findReadingProgressesByUser_EmailAndEndDateYear_noMatches_returnsEmptyList() {
        int nonMatchingYear = 1990;
        List<ReadingProgress> progresses = readingProgressRepository.findReadingProgressesByUser_EmailAndEndDateYear(
                login.getEmail(), nonMatchingYear);

        assertTrue(progresses.isEmpty());
    }

    @Test
    void findReadingProgressYearsByUser_Email_returnsDistinctNonNullCompletedYears() {
        List<Integer> years = readingProgressRepository.findReadingProgressYearsByUser_Email(login.getEmail());
        List<Integer> expectedYears = List.of(LocalDate.now().getYear(),
                LocalDate.now().minusYears(1).getYear());

        assertEquals(2, years.size());
        assertEquals(expectedYears, years);
        assertTrue(years.stream().allMatch(Objects::nonNull));
    }
}
