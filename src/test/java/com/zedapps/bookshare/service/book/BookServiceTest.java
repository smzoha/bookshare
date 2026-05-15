package com.zedapps.bookshare.service.book;

import com.zedapps.bookshare.dto.book.BookReviewDto;
import com.zedapps.bookshare.dto.book.ReviewLikeResponseDto;
import com.zedapps.bookshare.entity.book.Author;
import com.zedapps.bookshare.entity.book.Book;
import com.zedapps.bookshare.entity.book.Genre;
import com.zedapps.bookshare.entity.book.Tag;
import com.zedapps.bookshare.entity.login.*;
import com.zedapps.bookshare.enums.ActivityType;
import com.zedapps.bookshare.repository.book.BookRepository;
import com.zedapps.bookshare.repository.login.ReadingProgressRepository;
import com.zedapps.bookshare.repository.login.ReviewRepository;
import com.zedapps.bookshare.repository.login.ShelvedBookRepository;
import com.zedapps.bookshare.service.activity.ActivityService;
import com.zedapps.bookshare.service.auth.LoginDetails;
import com.zedapps.bookshare.service.login.LoginService;
import com.zedapps.bookshare.service.shelf.ShelfService;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * @author smzoha
 * @since 11/5/26
 **/
@ExtendWith(MockitoExtension.class)
public class BookServiceTest {

    @InjectMocks
    private BookService bookService;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ShelvedBookRepository shelvedBookRepository;

    @Mock
    private ReadingProgressRepository readingProgressRepository;

    @Mock
    private BookAdminService bookAdminService;

    @Mock
    private LoginService loginService;

    @Mock
    private ActivityService activityService;

    @Mock
    private ShelfService shelfService;

    private List<Book> books;
    private Genre genre;
    private Tag tag;
    private Review review;

    private Shelf defaultShelf1;
    private Shelf defaultShelf2;
    private Shelf defaultShelf3;

    private Shelf shelf1;
    private Shelf shelf2;
    private Shelf shelf3;

    private Login login;
    private LoginDetails loginDetails;

    @BeforeEach
    void setup() {
        login = TestUtils.getLogin("test@test.com", "test", true);
        loginDetails = TestUtils.getLoginDetails("test@test.com", "test", true);
        TestUtils.setupSecurityContext(loginDetails);

        Author author = TestUtils.getAuthor("Test", "Author");
        author.setId(1L);

        setupGenreTag();
        setupBooks(author);
        setupShelves();

        review = TestUtils.getReview(books.getFirst(), login, 5);
        review.setId(1L);

        Pageable pageable = PageRequest.of(0, 18);
        PageImpl<Book> bookPage = new PageImpl<>(books.subList(0, 18), pageable, books.size());

        lenient().when(bookRepository.getPaginatedBooks(any(Pageable.class), any(),
                any(), any(), any(), any(), any())).thenReturn(bookPage);

        lenient().when(loginService.getLogin(any())).thenReturn(login);
        lenient().when(bookAdminService.getBook(any())).thenReturn(books.getFirst());
        lenient().when(reviewRepository.save(any(Review.class))).thenReturn(review);

        lenient().when(shelfService.getShelvesForCollection(anyString())).thenReturn(
                List.of(defaultShelf1, defaultShelf2, defaultShelf3, shelf1, shelf2, shelf3));
    }

    @AfterEach
    void teardown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getPaginatedBooks_nullPageSize_defaultsToEighteen() {
        bookService.getPaginatedBooks(0, null, null, null, null, null, null);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(bookRepository).getPaginatedBooks(pageableCaptor.capture(), any(),
                any(), any(), any(), any(), any());

        assertEquals(18, pageableCaptor.getValue().getPageSize());
    }

    @Test
    void getPaginatedBooks_blankQuery_passesNullQueryToRepo() {
        bookService.getPaginatedBooks(0, null, "", null, null, null, null);

        ArgumentCaptor<String> queryCaptor = ArgumentCaptor.forClass(String.class);
        verify(bookRepository).getPaginatedBooks(any(Pageable.class), queryCaptor.capture(),
                any(), any(), any(), any(), any());

        assertNull(queryCaptor.getValue());
    }

    @Test
    void getPaginatedBooks_nonBlankQuery_wrapsQueryInLikePattern() {
        bookService.getPaginatedBooks(0, null, "Book", null, null, null, null);

        ArgumentCaptor<String> queryCaptor = ArgumentCaptor.forClass(String.class);
        verify(bookRepository).getPaginatedBooks(any(Pageable.class), queryCaptor.capture(),
                any(), any(), any(), any(), any());

        assertEquals("%book%", queryCaptor.getValue());
    }

    @Test
    void getPaginatedBooks_validSortString_parsesSortFieldAndDirection() {
        ArgumentCaptor<String> sortAttrCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> sortDirectionCaptor = ArgumentCaptor.forClass(String.class);

        bookService.getPaginatedBooks(0, null, null, "title,asc", null, null, null);
        verify(bookRepository).getPaginatedBooks(any(), any(), any(), any(), any(),
                sortAttrCaptor.capture(), sortDirectionCaptor.capture());

        assertEquals("title", sortAttrCaptor.getValue());
        assertEquals("asc", sortDirectionCaptor.getValue());
    }

    @Test
    void getPaginatedBooks_nullSort_passesNullSortComponentsToRepo() {
        ArgumentCaptor<String> sortAttrCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> sortDirectionCaptor = ArgumentCaptor.forClass(String.class);

        bookService.getPaginatedBooks(0, null, null, null, null, null, null);
        verify(bookRepository).getPaginatedBooks(any(), any(), any(), any(), any(),
                sortAttrCaptor.capture(), sortDirectionCaptor.capture());

        assertNull(sortAttrCaptor.getValue());
        assertNull(sortDirectionCaptor.getValue());
    }

    @Test
    void getRelatedBooks_matchingGenreOrTag_excludesOriginalBook() {
        List<Book> bookList = new ArrayList<>(books);
        Book targetBook = bookList.getFirst();

        when(bookRepository.getRelatedBooks(anySet(), anySet())).thenReturn(bookList);

        List<Book> retrievedRelatedBooks = bookService.getRelatedBooks(targetBook, Set.of(genre), Set.of(tag));

        assertFalse(retrievedRelatedBooks.contains(targetBook));
    }

    @Test
    void getReviewsByBook_existingBook_returnsPaginatedReviews() {
        Book book = books.getFirst();
        Page<Review> reviewPage = new PageImpl<>(List.of(review));

        when(reviewRepository.findReviewsByBookOrderByReviewDateDesc(eq(book), any(Pageable.class)))
                .thenReturn(reviewPage);

        Page<Review> reviews = bookService.getReviewsByBook(book, 0);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(reviewRepository).findReviewsByBookOrderByReviewDateDesc(eq(book), pageableCaptor.capture());

        assertEquals(5, pageableCaptor.getValue().getPageSize());
        assertEquals(reviewPage, reviews);
    }

    @Test
    void saveReview_validRequest_persistsAndReturnsReview() {
        Book book = books.getFirst();
        BookReviewDto reviewDto = new BookReviewDto(book.getId(), 5, "Review Content");

        bookService.saveReview(reviewDto, loginDetails);

        ArgumentCaptor<Review> reviewCaptor = ArgumentCaptor.forClass(Review.class);
        verify(reviewRepository).save(reviewCaptor.capture());

        assertEquals(book.getId(), reviewCaptor.getValue().getBook().getId());
        assertEquals(reviewDto.getRating(), reviewCaptor.getValue().getRating());
        assertEquals(reviewDto.getContent(), reviewCaptor.getValue().getContent());
        assertEquals(login, reviewCaptor.getValue().getUser());
    }

    @Test
    void saveReview_validRequest_firesBookAddReviewOutbox() {
        Book book = books.getFirst();
        BookReviewDto reviewDto = new BookReviewDto(book.getId(), 5, "Review Content");

        bookService.saveReview(reviewDto, loginDetails);

        verify(activityService).saveActivityOutbox(eq(ActivityType.BOOK_ADD_REVIEW),
                eq(review.getId()), anyMap());
    }

    @Test
    void updateReviewLikes_reviewNotFound_throwsNoResultException() {
        when(reviewRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NoResultException.class, () -> bookService.updateReviewLikes(1L, loginDetails));
    }

    @Test
    void updateReviewLikes_loginNotInLikes_addsLikeAndFiresLikeOutbox() {
        when(reviewRepository.findById(anyLong())).thenReturn(Optional.of(review));

        ReviewLikeResponseDto reviewLikeResponseDto = bookService.updateReviewLikes(1L, loginDetails);

        assertNotNull(reviewLikeResponseDto);
        assertTrue(reviewLikeResponseDto.getLiked());
        assertTrue(review.getUserLikes().contains(login));

        verify(activityService).saveActivityOutbox(eq(ActivityType.BOOK_LIKE_REVIEW),
                eq(review.getId()), anyMap());
    }

    @Test
    void updateReviewLikes_loginAlreadyInLikes_removesLikeAndFiresRemoveLikeOutbox() {
        when(reviewRepository.findById(anyLong())).thenReturn(Optional.of(review));

        review.getUserLikes().add(login);

        ReviewLikeResponseDto reviewLikeResponseDto = bookService.updateReviewLikes(1L, loginDetails);

        assertNotNull(reviewLikeResponseDto);
        assertFalse(reviewLikeResponseDto.getLiked());
        assertFalse(review.getUserLikes().contains(login));

        verify(activityService).saveActivityOutbox(eq(ActivityType.BOOK_REMOVE_LIKE_REVIEW),
                eq(review.getId()), anyMap());
    }

    @Test
    void addToShelf_defaultShelf_removesBookFromOtherDefaultShelves() {
        Book book = books.getFirst();

        ShelvedBook existingShelvedBook = TestUtils.getShelvedBook(book, login, defaultShelf2);
        existingShelvedBook.setId(1L);

        defaultShelf2.getBooks().add(existingShelvedBook);

        ShelvedBook savedShelvedBook = TestUtils.getShelvedBook(book, login, defaultShelf1);
        savedShelvedBook.setId(2L);

        when(shelvedBookRepository.save(any(ShelvedBook.class))).thenReturn(savedShelvedBook);
        when(shelvedBookRepository.findShelvedBookByLoginAndShelfAndBook(eq(login), eq(defaultShelf2),
                eq(book))).thenReturn(Optional.of(existingShelvedBook));

        bookService.addToShelf(loginDetails, book.getId(), defaultShelf1.getId());

        ArgumentCaptor<ShelvedBook> shelvedBookCaptor = ArgumentCaptor.forClass(ShelvedBook.class);
        verify(shelvedBookRepository).save(shelvedBookCaptor.capture());

        assertEquals(defaultShelf1, shelvedBookCaptor.getValue().getShelf());
        assertEquals(book, shelvedBookCaptor.getValue().getBook());

        verify(shelvedBookRepository).delete(existingShelvedBook);
    }

    @Test
    void addToShelf_nonDefaultShelf_doesNotRemoveFromOtherShelves() {
        Book book = books.getFirst();

        ShelvedBook savedShelvedBook = TestUtils.getShelvedBook(book, login, shelf1);
        savedShelvedBook.setId(2L);

        when(shelvedBookRepository.save(any(ShelvedBook.class))).thenReturn(savedShelvedBook);

        bookService.addToShelf(loginDetails, book.getId(), shelf1.getId());

        verify(shelvedBookRepository, never()).delete(any(ShelvedBook.class));
    }

    @Test
    void addToShelf_validRequest_firesAddToShelfOutbox() {
        Book book = books.getFirst();

        ShelvedBook savedShelvedBook = TestUtils.getShelvedBook(book, login, shelf1);
        savedShelvedBook.setId(2L);

        when(shelvedBookRepository.save(any(ShelvedBook.class))).thenReturn(savedShelvedBook);

        bookService.addToShelf(loginDetails, book.getId(), shelf1.getId());

        verify(activityService).saveActivityOutbox(eq(ActivityType.BOOK_ADD_TO_SHELF),
                eq(shelf1.getId()), anyMap());
    }

    @Test
    void removeFromShelf_shelvedBookNotFound_throwsNoSuchElementException() {
        when(shelvedBookRepository.findShelvedBookByLoginAndShelfAndBook(any(Login.class),
                any(Shelf.class), any(Book.class))).thenReturn(Optional.empty());

        login.setShelves(Set.of(defaultShelf1));

        assertThrows(NoSuchElementException.class, () -> bookService.removeFromShelf(loginDetails, 1L, 1L));
    }

    @Test
    void removeFromShelf_validRequest_deletesAndFiresOutbox() {
        Book book = books.getFirst();

        ShelvedBook existingShelvedBook = TestUtils.getShelvedBook(book, login, shelf1);
        existingShelvedBook.setId(1L);

        shelf1.getBooks().add(existingShelvedBook);

        when(shelvedBookRepository.findShelvedBookByLoginAndShelfAndBook(login, shelf1, book))
                .thenReturn(Optional.of(existingShelvedBook));

        login.setShelves(Set.of(shelf1));

        bookService.removeFromShelf(loginDetails, 1L, 4L);

        verify(shelvedBookRepository).delete(existingShelvedBook);
        verify(activityService).saveActivityOutbox(eq(ActivityType.BOOK_REMOVE_FROM_SHELF),
                eq(shelf1.getId()), anyMap());
    }

    @Test
    void saveReadingProgress_existingIdWithMatchingUser_updatesRecord() {
        Book book = books.getFirst();
        ReadingProgress readingProgress = new ReadingProgress(1L, login, book, 2L, LocalDate.now(),
                null, false, LocalDateTime.now());

        when(readingProgressRepository.findById(anyLong())).thenReturn(Optional.of(readingProgress));
        when(readingProgressRepository.save(any(ReadingProgress.class))).thenReturn(readingProgress);

        ReadingProgress updatedReadingProgress = bookService.saveReadingProgress(readingProgress, loginDetails);

        verify(readingProgressRepository).save(any(ReadingProgress.class));
        assertEquals(readingProgress.getId(), updatedReadingProgress.getId());
        assertEquals(readingProgress.getUser(), updatedReadingProgress.getUser());
    }

    @Test
    void saveReadingProgress_existingIdWithMismatchedUser_throwsAssertionError() {
        Book book = books.getFirst();

        Login otherLogin = TestUtils.getLogin("test2@test.com", "test2", true);
        ReadingProgress readingProgress = new ReadingProgress(1L, otherLogin, book, 2L, LocalDate.now(),
                null, false, LocalDateTime.now());


        when(readingProgressRepository.findById(anyLong())).thenReturn(Optional.of(readingProgress));

        assertThrows(AssertionError.class, () -> bookService.saveReadingProgress(readingProgress, loginDetails));
    }

    @Test
    void saveReadingProgress_completedProgress_setsPagesReadToBookTotal() {
        ReadingProgress readingProgress = saveCompletedReadingProgress();

        assertEquals(books.getFirst().getPages(), readingProgress.getPagesRead());
    }

    @Test
    void saveReadingProgress_completedProgressWithNullEndDate_setsEndDateToNow() {
        ReadingProgress readingProgress = saveCompletedReadingProgress();

        assertEquals(LocalDate.now(), readingProgress.getEndDate());
    }

    @Test
    void saveReadingProgress_validRequest_firesReadingProgressOutbox() {
        ReadingProgress readingProgress = saveCompletedReadingProgress();

        verify(activityService).saveActivityOutbox(eq(ActivityType.BOOK_UPDATE_READING_PROGRESS),
                eq(readingProgress.getId()), anyMap());
    }

    private void setupBooks(Author author) {
        books = TestUtils.getBooks(author, Set.of(genre), Set.of(tag));
    }

    private void setupGenreTag() {
        genre = TestUtils.getGenre("Genre");
        genre.setId(1L);

        tag = TestUtils.getTag("Tag");
        tag.setId(1L);
    }

    private void setupShelves() {
        defaultShelf1 = TestUtils.getShelf(login, "Currently Reading", true);
        defaultShelf1.setId(1L);

        defaultShelf2 = TestUtils.getShelf(login, "Reading", true);
        defaultShelf2.setId(2L);

        defaultShelf3 = TestUtils.getShelf(login, "Read", true);
        defaultShelf3.setId(3L);

        shelf1 = TestUtils.getShelf(login, "Action", false);
        shelf1.setId(4L);

        shelf2 = TestUtils.getShelf(login, "Adventure", false);
        shelf2.setId(5L);

        shelf3 = TestUtils.getShelf(login, "Fantasy", false);
        shelf3.setId(6L);

        login.setShelves(Set.of(defaultShelf1, defaultShelf2, defaultShelf3, shelf1, shelf2, shelf3));
    }

    private ReadingProgress saveCompletedReadingProgress() {
        Book book = books.getFirst();
        ReadingProgress readingProgress = new ReadingProgress(1L, login, book, 2L, LocalDate.now(),
                null, true, LocalDateTime.now());

        when(readingProgressRepository.findById(anyLong())).thenReturn(Optional.of(readingProgress));
        when(readingProgressRepository.save(any(ReadingProgress.class))).thenReturn(readingProgress);

        return bookService.saveReadingProgress(readingProgress, loginDetails);
    }
}
