package com.zedapps.bookshare.service.book;

import com.zedapps.bookshare.dto.api.book.*;
import com.zedapps.bookshare.dto.book.BookReviewDto;
import com.zedapps.bookshare.entity.book.Author;
import com.zedapps.bookshare.entity.book.Book;
import com.zedapps.bookshare.entity.book.Genre;
import com.zedapps.bookshare.entity.book.Tag;
import com.zedapps.bookshare.entity.login.*;
import com.zedapps.bookshare.repository.login.ReadingProgressRepository;
import com.zedapps.bookshare.repository.login.ReviewRepository;
import com.zedapps.bookshare.repository.login.ShelfRepository;
import com.zedapps.bookshare.service.auth.LoginDetails;
import com.zedapps.bookshare.util.TestUtils;
import jakarta.persistence.NoResultException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * @author smzoha
 * @since 15/5/26
 **/
@ExtendWith(MockitoExtension.class)
public class BookApiServiceTest {

    @InjectMocks
    private BookApiService bookApiService;

    @Mock
    private BookService bookService;

    @Mock
    private ShelfRepository shelfRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ReadingProgressRepository readingProgressRepository;

    private Author author;
    private Genre genre;
    private Tag tag;
    private List<Book> books;
    private Review review;

    private Login login;
    private Shelf shelf;
    private LoginDetails loginDetails;

    @BeforeEach
    void setUp() {
        login = TestUtils.getLogin("test@test.com", "test", true);
        login.setId(1L);

        loginDetails = TestUtils.getLoginDetails(login.getEmail(), login.getHandle(), login.isActive());
        TestUtils.setupSecurityContext(loginDetails);

        author = TestUtils.getAuthor("Test", "Author");
        author.setId(1L);

        genre = TestUtils.getGenre("Genre");
        genre.setId(1L);

        tag = TestUtils.getTag("Tag");
        tag.setId(1L);

        books = TestUtils.getBooks(author, Set.of(genre), Set.of(tag));

        shelf = TestUtils.getShelf(login, "Shelf 1", true);
        shelf.setId(1L);

        review = TestUtils.getReview(books.getFirst(), login, 5);
        review.setId(1L);

        lenient().when(bookService.getBook(anyLong())).thenReturn(books.getFirst());
        lenient().when(shelfRepository.findById(1L)).thenReturn(Optional.of(shelf));
        lenient().when(reviewRepository.findById(anyLong())).thenReturn(Optional.of(review));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getBookDto_existingBook_returnsMappedDto() {
        Book book = books.getFirst();
        BookDto bookDto = bookApiService.getBookDto(book.getId(), false);

        assertEquals(book.getTitle(), bookDto.title());
        assertEquals(book.getIsbn(), bookDto.isbn());
        assertEquals(author.getFirstName(), bookDto.authors().getFirst().firstName());
        assertEquals(author.getLastName(), bookDto.authors().getLast().lastName());
        assertEquals(genre.getName(), bookDto.genres().getFirst());
        assertEquals(tag.getName(), bookDto.tags().getFirst());
    }

    @Test
    void getBookDto_missingBook_returnsNull() {
        when(bookService.getBook(2L)).thenThrow(NoResultException.class);

        assertNull(bookApiService.getBookDto(2L, false));
    }

    @Test
    void getBookDto_withReviewsFlag_includesReviewsInDto() {
        Book book = books.getFirst();
        book.setReviews(Set.of(review));

        BookDto bookDto = bookApiService.getBookDto(book.getId(), true);

        assertNotNull(bookDto.reviews());

        ReviewDto reviewDto = bookDto.reviews().getFirst();
        assertEquals(login.getName(), reviewDto.reviewedBy());
        assertEquals(review.getReviewDate(), reviewDto.reviewDate());
        assertEquals(review.getRating(), reviewDto.rating());
        assertEquals(review.getContent(), reviewDto.comment());
    }

    @Test
    void getBookDto_withoutReviewsFlag_excludesReviewsFromDto() {
        Book book = books.getFirst();
        BookDto bookDto = bookApiService.getBookDto(book.getId(), false);

        assertTrue(bookDto.reviews().isEmpty());
    }

    @Test
    void isInvalidShelfRequest_shelfNotFound_returnsTrue() {
        when(shelfRepository.findById(anyLong())).thenReturn(Optional.empty());

        boolean isInvalid = bookApiService.isInvalidShelfRequest(null, 1L, loginDetails);
        assertTrue(isInvalid);
    }

    @Test
    void isInvalidShelfRequest_shelfBelongsToOtherUser_returnsTrue() {
        Login otherLogin = TestUtils.getLogin("other@test.com", "other", true);
        Shelf shelf = TestUtils.getShelf(otherLogin, "Shelf 1", true);

        when(shelfRepository.findById(anyLong())).thenReturn(Optional.of(shelf));

        boolean isInvalid = bookApiService.isInvalidShelfRequest(null, 1L, loginDetails);
        assertTrue(isInvalid);
    }

    @Test
    void isInvalidShelfRequest_validShelfAndNullBookId_returnsFalse() {
        boolean isInvalid = bookApiService.isInvalidShelfRequest(null, 1L, loginDetails);

        assertFalse(isInvalid);
    }

    @Test
    void isInvalidShelfRequest_validShelfAndBookNotInShelf_returnsTrue() {
        Book book = books.getFirst();

        boolean isInvalid = bookApiService.isInvalidShelfRequest(book.getId(), 1L, loginDetails);

        assertTrue(isInvalid);
    }

    @Test
    void isInvalidShelfRequest_validShelfAndBookInShelf_returnsFalse() {
        Book book = books.getFirst();
        ShelvedBook shelvedBook = TestUtils.getShelvedBook(book, login, shelf);

        shelf.setBooks(Set.of(shelvedBook));

        boolean isInvalid = bookApiService.isInvalidShelfRequest(book.getId(), 1L, loginDetails);

        assertFalse(isInvalid);
    }

    @Test
    void isValidReviewRequest_existingReview_returnsTrue() {
        boolean isValid = bookApiService.isValidReviewRequest(review.getId());

        assertTrue(isValid);
    }

    @Test
    void isValidReviewRequest_missingReview_returnsFalse() {
        when(reviewRepository.findById(anyLong())).thenReturn(Optional.empty());

        boolean isValid = bookApiService.isValidReviewRequest(review.getId());

        assertFalse(isValid);
    }

    @Test
    void createDto_nullImageUrl_setsEmptyImageUrl() {
        Book book = books.getFirst();
        BookDto bookDto = bookApiService.createDto(book, false);

        assertNull(book.getImage());
        assertTrue(bookDto.imageUrl().isEmpty());
    }

    @Test
    void createDto_htmlDescription_stripsHtmlTags() {
        Book book = books.getFirst();
        book.setDescription("<h1>Test<h1>");

        BookDto bookDto = bookApiService.createDto(book, false);

        assertNotEquals(book.getDescription(), bookDto.description());
        assertEquals("Test", bookDto.description());
    }

    @Test
    void saveReview_validRequest_returnsReviewDto() {
        Book book = books.getFirst();

        ReviewRequest reviewRequest = new ReviewRequest(5, "Review Content");

        Review savedReview = TestUtils.getReview(book, login, 5);

        when(bookService.saveReview(any(BookReviewDto.class),
                eq(loginDetails))).thenReturn(savedReview);

        ReviewDto reviewDto = bookApiService.saveReview(book.getId(), reviewRequest, loginDetails);

        ArgumentCaptor<BookReviewDto> captor = ArgumentCaptor.forClass(BookReviewDto.class);
        verify(bookService).saveReview(captor.capture(), eq(loginDetails));

        assertEquals(book.getId(), captor.getValue().getBookId());
        assertEquals(reviewRequest.rating(), captor.getValue().getRating());
        assertEquals(reviewRequest.content(), captor.getValue().getContent());

        assertEquals(reviewRequest.content(), reviewDto.comment());
        assertEquals(reviewRequest.rating(), reviewDto.rating());
        assertEquals(login.getName(), reviewDto.reviewedBy());
    }

    @Test
    void saveReadingProgress_withExistingProgressId_loadsAndUpdatesRecord() {
        Book book = books.getFirst();

        ReadingProgressRequest rpRequest = new ReadingProgressRequest(1L, 10L,
                LocalDate.now(), null, false);

        ReadingProgress readingProgress = new ReadingProgress(1L, login, book, 5L, LocalDate.now(), null,
                false, LocalDateTime.now());

        ReadingProgress savedReadingProgress = new ReadingProgress(1L, login, book, 10L,
                rpRequest.startDate(), null,
                false, LocalDateTime.now());

        when(readingProgressRepository.findById(readingProgress.getId()))
                .thenReturn(Optional.of(readingProgress));

        when(bookService.saveReadingProgress(same(readingProgress), eq(loginDetails))).thenReturn(savedReadingProgress);

        ReadingProgressDto progressDto = bookApiService.saveReadingProgress(book.getId(), rpRequest, loginDetails);

        verify(readingProgressRepository).findById(rpRequest.progressId());

        assertEquals(book.getTitle(), progressDto.bookTitle());
        assertEquals(book.getIsbn(), progressDto.isbn());
        assertEquals(login.getName(), progressDto.login());

        assertEquals(rpRequest.pagesRead(), progressDto.pagesRead());
        assertEquals(rpRequest.completed(), progressDto.completed());
        assertEquals(rpRequest.startDate(), progressDto.startDate());
    }

    @Test
    void saveReadingProgress_withoutProgressId_createsNewRecord() {
        Book book = books.getFirst();

        ReadingProgressRequest rpRequest = new ReadingProgressRequest(null, 10L,
                LocalDate.now(), null, false);

        ReadingProgress readingProgress = new ReadingProgress(1L, login, book, 10L,
                rpRequest.startDate(), null,
                false, LocalDateTime.now());

        when(bookService.saveReadingProgress(any(), eq(loginDetails)))
                .thenReturn(readingProgress);

        ReadingProgressDto progressDto = bookApiService.saveReadingProgress(book.getId(), rpRequest, loginDetails);

        verify(readingProgressRepository, never()).findById(anyLong());
        assertEquals(book.getTitle(), progressDto.bookTitle());
        assertEquals(book.getIsbn(), progressDto.isbn());
        assertEquals(login.getName(), progressDto.login());

        assertEquals(rpRequest.pagesRead(), progressDto.pagesRead());
        assertEquals(rpRequest.completed(), progressDto.completed());
        assertEquals(rpRequest.startDate(), progressDto.startDate());
    }
}
