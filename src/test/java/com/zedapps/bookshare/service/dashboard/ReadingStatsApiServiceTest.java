package com.zedapps.bookshare.service.dashboard;

import com.zedapps.bookshare.dto.api.book.AuthorDto;
import com.zedapps.bookshare.dto.api.book.BookDto;
import com.zedapps.bookshare.dto.api.dashboard.ReadingStatsDto;
import com.zedapps.bookshare.entity.book.Author;
import com.zedapps.bookshare.entity.book.Book;
import com.zedapps.bookshare.enums.Status;
import com.zedapps.bookshare.helper.ReadingStatsHelper;
import com.zedapps.bookshare.service.auth.LoginDetails;
import com.zedapps.bookshare.service.book.BookApiService;
import com.zedapps.bookshare.util.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * @author smzoha
 * @since 26/6/26
 **/
@ExtendWith(MockitoExtension.class)
class ReadingStatsApiServiceTest {

    @Mock
    private ReadingStatsHelper readingStatsHelper;

    @Mock
    private BookApiService bookApiService;

    @InjectMocks
    private ReadingStatsApiService readingStatsApiService;

    private LoginDetails loginDetails;

    @BeforeEach
    void setUp() {
        loginDetails = TestUtils.getLoginDetails("test@test.com", "test", true);
        TestUtils.setupSecurityContext(loginDetails);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getReadingStatsDto_populatedModel_mapsEveryFieldFromHelper() {
        Author author = TestUtils.getAuthor("Test", "Author");
        author.setId(1L);

        Book longBook = TestUtils.getBook("Longest Book", TestUtils.TEST_ISBN_DATA.getFirst(), author, Status.ACTIVE);
        longBook.setId(1L);

        Book shortBook = TestUtils.getBook("Short Book", TestUtils.TEST_ISBN_DATA.get(1), author, Status.ACTIVE);
        shortBook.setId(2L);

        Book latestBook = TestUtils.getBook("Latest Book", TestUtils.TEST_ISBN_DATA.get(2), author, Status.ACTIVE);
        latestBook.setId(3L);

        stubSetupReadingReferenceData(longBook, shortBook, latestBook);
        stubCreateBookDto(author, longBook, shortBook, latestBook);

        ReadingStatsDto readingStatsDto = readingStatsApiService.getReadingStatsDto(loginDetails, 2026);

        assertEquals(10, readingStatsDto.challengeBookCount());
        assertEquals(5, readingStatsDto.booksReadCount());
        assertEquals(6, readingStatsDto.readCount());
        assertEquals(1200L, readingStatsDto.totalPagesRead());
        assertEquals(7L, readingStatsDto.averageFinishTime());
        assertEquals(9L, readingStatsDto.avgPagesPerDay());

        assertEquals(Map.of("JANUARY", 1), readingStatsDto.booksReadMap());

        assertEquals(longBook.getTitle(), readingStatsDto.longestBook().title());
        assertEquals(longBook.getIsbn(), readingStatsDto.longestBook().isbn());

        assertEquals(shortBook.getTitle(), readingStatsDto.shortestBook().title());
        assertEquals(shortBook.getIsbn(), readingStatsDto.shortestBook().isbn());

        assertEquals(latestBook.getTitle(), readingStatsDto.latestBook().title());
        assertEquals(latestBook.getIsbn(), readingStatsDto.latestBook().isbn());

        assertEquals(3, readingStatsDto.reviewCount());
        assertEquals(4, readingStatsDto.totalAvgReview());

        assertEquals(Map.of(5, 2), readingStatsDto.ratingMap());
    }

    @Test
    void getReadingStatsDto_nullBooks_leavesBookDtosNullAndSkipsCreateDto() {
        stubSetupReadingReferenceData(null, null, null);

        ReadingStatsDto readingStatsDto = readingStatsApiService.getReadingStatsDto(loginDetails, 2026);

        assertNull(readingStatsDto.longestBook());
        assertNull(readingStatsDto.shortestBook());
        assertNull(readingStatsDto.latestBook());

        verify(bookApiService, never()).createDto(any(Book.class), eq(false));
    }

    private void stubSetupReadingReferenceData(Book longBook, Book shortBook, Book latestBook) {
        doAnswer(invocation -> {
            Map<String, Object> model = invocation.getArgument(2);
            model.put("challengeBookCount", 10);
            model.put("booksReadCount", 5);
            model.put("readCount", 6);
            model.put("totalPagesRead", 1200L);
            model.put("averageFinishTime", 7L);
            model.put("avgPagesPerDay", 9L);
            model.put("booksReadMap", Map.of("JANUARY", 1));
            model.put("longestBook", longBook);
            model.put("shortestBook", shortBook);
            model.put("latestBook", latestBook);
            model.put("reviewCount", 3);
            model.put("totalAvgReview", 4);
            model.put("ratingMap", Map.of(5, 2));

            return null;
        }).when(readingStatsHelper).setupReadingStatsReferenceData(eq(loginDetails), eq(2026), anyMap());
    }

    private void stubCreateBookDto(Author author, Book longBook, Book shortBook, Book latestBook) {
        AuthorDto authorDto = new AuthorDto(author.getFirstName(), author.getLastName(), null);

        when(bookApiService.createDto(longBook, false))
                .thenReturn(new BookDto(longBook.getTitle(), longBook.getIsbn(), longBook.getDescription(), null,
                        longBook.getPages(), longBook.getPublicationDate(), longBook.getAverageRating(),
                        List.of(authorDto), List.of(), List.of(), List.of()));

        when(bookApiService.createDto(shortBook, false))
                .thenReturn(new BookDto(shortBook.getTitle(), shortBook.getIsbn(), shortBook.getDescription(), null,
                        shortBook.getPages(), shortBook.getPublicationDate(), shortBook.getAverageRating(),
                        List.of(authorDto), List.of(), List.of(), List.of()));

        when(bookApiService.createDto(latestBook, false))
                .thenReturn(new BookDto(latestBook.getTitle(), latestBook.getIsbn(), latestBook.getDescription(), null,
                        latestBook.getPages(), latestBook.getPublicationDate(), latestBook.getAverageRating(),
                        List.of(authorDto), List.of(), List.of(), List.of()));
    }
}
