package com.zedapps.bookshare.helper;

import com.zedapps.bookshare.entity.book.Author;
import com.zedapps.bookshare.entity.book.Book;
import com.zedapps.bookshare.entity.login.Login;
import com.zedapps.bookshare.enums.Status;
import com.zedapps.bookshare.repository.login.ReadingChallengeRepository;
import com.zedapps.bookshare.repository.login.ReadingProgressRepository;
import com.zedapps.bookshare.repository.login.ReviewRepository;
import com.zedapps.bookshare.service.auth.LoginDetails;
import com.zedapps.bookshare.util.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author smzoha
 * @since 26/6/26
 **/
@ExtendWith(MockitoExtension.class)
class ReadingStatsHelperTest {

    @Mock
    private ReadingChallengeRepository readingChallengeRepository;

    @Mock
    private ReadingProgressRepository readingProgressRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @InjectMocks
    private ReadingStatsHelper readingStatsHelper;

    private LoginDetails loginDetails;

    private Login login;
    private Book longBook;
    private Book shortBook;
    private Book latestBook;

    @BeforeEach
    void setUp() {
        loginDetails = TestUtils.getLoginDetails("test@test.com", "test", true);

        login = TestUtils.getLogin("test@test.com", "test", true);
        login.setId(1L);

        Author author = TestUtils.getAuthor("Test", "Author");

        setUpBooks(author);
        setUpReadingProgresses();
        setUpReviews();
    }

    @Test
    @SuppressWarnings("unchecked")
    void setupReadingStatsReferenceData_happyPath_populatesAllAggregateKeys() {
        int year = 2025;

        when(readingChallengeRepository.getReadingChallengeByLogin_EmailAndYear(login.getEmail(), year))
                .thenReturn(Optional.of(TestUtils.getReadingChallenge(login, year, 10)));

        when(readingProgressRepository.findReadingProgressYearsByUser_Email(login.getEmail()))
                .thenReturn(List.of(year));

        when(readingProgressRepository.findReadingProgressesByUser_EmailAndEndDateYear(login.getEmail(), year))
                .thenReturn(new ArrayList<>(login.getReadingProgresses()));

        when(reviewRepository.findReviewsByUser_EmailAndReviewDateYear(login.getEmail(), year))
                .thenReturn(new ArrayList<>(login.getReviews()));

        Map<String, Object> model = new HashMap<>();
        readingStatsHelper.setupReadingStatsReferenceData(loginDetails, year, model);

        assertEquals(10, model.get("challengeBookCount"));
        assertEquals(List.of(year), model.get("readingProgressYears"));

        assertEquals(3, model.get("booksReadCount"));
        assertEquals(4, model.get("readCount"));
        assertEquals(1150L, model.get("totalPagesRead"));

        assertEquals((long) Math.ceilDiv(26, login.getReadingProgresses().size()), model.get("averageFinishTime"));
        assertEquals(Math.ceilDiv(1150L, 365), model.get("avgPagesPerDay"));

        Map<String, Integer> booksReadMap = (Map<String, Integer>) model.get("booksReadMap");
        assertEquals(12, booksReadMap.size());
        assertEquals(2, booksReadMap.get("JANUARY"));
        assertEquals(1, booksReadMap.get("FEBRUARY"));
        assertEquals(1, booksReadMap.get("MARCH"));
        assertEquals(0, booksReadMap.get("APRIL"));

        assertEquals(longBook, model.get("longestBook"));
        assertEquals(shortBook, model.get("shortestBook"));
        assertEquals(latestBook, model.get("latestBook"));

        assertEquals(login.getReviews().size(), model.get("reviewCount"));
        assertEquals(Math.ceilDiv(15, login.getReviews().size()), model.get("totalAvgReview"));

        Map<Integer, Integer> ratingMap = (Map<Integer, Integer>) model.get("ratingMap");
        assertEquals(5, ratingMap.size());
        assertEquals(3, ratingMap.get(5));
        assertEquals(0, ratingMap.get(4));
        assertEquals(0, ratingMap.get(1));
    }

    @Test
    void setupReadingStatsReferenceData_missingChallenge_setsChallengeBookCountToZero() {
        stubEmptyStats();

        Map<String, Object> model = new HashMap<>();
        readingStatsHelper.setupReadingStatsReferenceData(loginDetails, 2025, model);

        assertEquals(0, model.get("challengeBookCount"));
    }

    @Test
    void setupReadingStatsReferenceData_nullYear_defaultsToCurrentYear() {
        stubEmptyStats();

        int currentYear = LocalDate.now().getYear();

        Map<String, Object> model = new HashMap<>();
        readingStatsHelper.setupReadingStatsReferenceData(loginDetails, null, model);

        verify(readingChallengeRepository).getReadingChallengeByLogin_EmailAndYear(
                eq(login.getEmail()), eq(currentYear));

        verify(readingProgressRepository).findReadingProgressesByUser_EmailAndEndDateYear(
                eq(login.getEmail()), eq(currentYear));

        verify(reviewRepository).findReviewsByUser_EmailAndReviewDateYear(
                eq(login.getEmail()), eq(currentYear));
    }

    @Test
    @SuppressWarnings("unchecked")
    void setupReadingStatsReferenceData_noProgressOrReviews_setsZeroedStatsAndNullLatestBook() {
        stubEmptyStats();

        Map<String, Object> model = new HashMap<>();
        readingStatsHelper.setupReadingStatsReferenceData(loginDetails, 2025, model);

        assertEquals(0, model.get("challengeBookCount"));
        assertEquals(List.of(), model.get("readingProgressYears"));

        assertEquals(0, model.get("booksReadCount"));
        assertEquals(0, model.get("readCount"));
        assertEquals(0L, model.get("totalPagesRead"));

        assertEquals(0L, model.get("averageFinishTime"));
        assertEquals(0L, model.get("avgPagesPerDay"));

        Map<String, Integer> booksReadMap = (Map<String, Integer>) model.get("booksReadMap");
        assertEquals(12, booksReadMap.size());

        assertNull(model.get("longestBook"));
        assertNull(model.get("shortestBook"));
        assertNull(model.get("latestBook"));

        assertEquals(0, model.get("reviewCount"));
        assertEquals(0, model.get("totalAvgReview"));

        Map<Integer, Integer> ratingMap = (Map<Integer, Integer>) model.get("ratingMap");
        assertEquals(5, ratingMap.size());
    }

    private void setUpBooks(Author author) {
        longBook = TestUtils.getBook("Long Book", TestUtils.TEST_ISBN_DATA.getFirst(), author, Status.ACTIVE);
        longBook.setPages(500L);
        longBook.setId(1L);

        shortBook = TestUtils.getBook("Short Book", TestUtils.TEST_ISBN_DATA.get(1), author, Status.ACTIVE);
        shortBook.setPages(50L);
        shortBook.setId(2L);

        latestBook = TestUtils.getBook("Latest Book", TestUtils.TEST_ISBN_DATA.get(2), author, Status.ACTIVE);
        latestBook.setId(3L);
    }

    private void setUpReadingProgresses() {
        login.getReadingProgresses().add(TestUtils.getReadingProgress(longBook, login, longBook.getPages(),
                LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 10), true));

        login.getReadingProgresses().add(TestUtils.getReadingProgress(shortBook, login, shortBook.getPages(),
                LocalDate.of(2025, 2, 1), LocalDate.of(2025, 2, 3), true));

        login.getReadingProgresses().add(TestUtils.getReadingProgress(longBook, login, longBook.getPages(),
                LocalDate.of(2025, 1, 15), LocalDate.of(2025, 1, 20), true));

        login.getReadingProgresses().add(TestUtils.getReadingProgress(latestBook, login, latestBook.getPages(),
                LocalDate.of(2025, 3, 1), LocalDate.of(2025, 3, 11), true));
    }

    private void setUpReviews() {
        Stream.of(longBook, shortBook, latestBook)
                .map(book -> TestUtils.getReview(book, login, 5))
                .forEach(login.getReviews()::add);
    }

    private void stubEmptyStats() {
        when(readingChallengeRepository.getReadingChallengeByLogin_EmailAndYear(eq(login.getEmail()), anyInt()))
                .thenReturn(Optional.empty());

        when(readingProgressRepository.findReadingProgressYearsByUser_Email(eq(login.getEmail()))).thenReturn(List.of());
        when(readingProgressRepository.findReadingProgressesByUser_EmailAndEndDateYear(eq(login.getEmail()), anyInt()))
                .thenReturn(List.of());

        when(reviewRepository.findReviewsByUser_EmailAndReviewDateYear(eq(login.getEmail()), anyInt()))
                .thenReturn(List.of());
    }
}
