package com.zedapps.bookshare.helper;

import com.zedapps.bookshare.dto.book.BookReviewDto;
import com.zedapps.bookshare.entity.book.Author;
import com.zedapps.bookshare.entity.book.Book;
import com.zedapps.bookshare.entity.book.Genre;
import com.zedapps.bookshare.entity.book.Tag;
import com.zedapps.bookshare.entity.login.Login;
import com.zedapps.bookshare.entity.login.ReadingProgress;
import com.zedapps.bookshare.entity.login.Review;
import com.zedapps.bookshare.entity.login.Shelf;
import com.zedapps.bookshare.enums.Status;
import com.zedapps.bookshare.service.auth.LoginDetails;
import com.zedapps.bookshare.service.book.BookService;
import com.zedapps.bookshare.service.login.LoginService;
import com.zedapps.bookshare.service.shelf.ShelfService;
import com.zedapps.bookshare.util.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.ui.ModelMap;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

/**
 * @author smzoha
 * @since 3/6/26
 **/
@ExtendWith(MockitoExtension.class)
class BookHelperTest {

    @Mock
    private BookService bookService;

    @Mock
    private LoginService loginService;

    @Mock
    private ShelfService shelfService;

    @InjectMocks
    private BookHelper bookHelper;

    private Login login;

    private Book book;
    private Review review;

    private Shelf defaultShelf;
    private Shelf customShelf;
    private ReadingProgress readingProgress;

    @BeforeEach
    void setUp() {
        login = TestUtils.getLogin("test@test.com", "test", true);
        login.setId(1L);

        Author author = TestUtils.getAuthor("Test", "Author");
        author.setId(1L);

        Genre genre = TestUtils.getGenre("Genre");
        genre.setId(1L);

        Tag tag = TestUtils.getTag("Tag");
        tag.setId(1L);

        book = TestUtils.getBook("Book 1", "9780743273565", author, Status.ACTIVE);
        book.setId(1L);
        book.setGenres(Set.of(genre));
        book.setTags(Set.of(tag));

        readingProgress = new ReadingProgress();
        readingProgress.setId(1L);
        readingProgress.setBook(book);
        readingProgress.setUser(login);
        readingProgress.setPagesRead(10L);
        readingProgress.setStartDate(LocalDate.now());
        login.setReadingProgresses(Set.of(readingProgress));

        review = TestUtils.getReview(book, login, 5);
        book.setReviews(Set.of(review));

        defaultShelf = TestUtils.getShelf(login, Shelf.SHELF_READ, true);
        defaultShelf.setBooks(Set.of(TestUtils.getShelvedBook(book, login, defaultShelf)));
        defaultShelf.setId(1L);

        customShelf = TestUtils.getShelf(login, "Custom", false);
        customShelf.setBooks(Set.of(TestUtils.getShelvedBook(book, login, customShelf)));
        customShelf.setId(2L);

        lenient().when(loginService.getLogin(login.getEmail())).thenReturn(login);
        lenient().when(shelfService.getShelvesForCollection(login.getEmail())).thenReturn(List.of(defaultShelf, customShelf));
        lenient().when(bookService.getBook(book.getId())).thenReturn(book);
        lenient().when(bookService.getReviewsByBook(book, 0)).thenReturn(new PageImpl<>(List.of(review)));
        lenient().when(bookService.getRelatedBooks(book, book.getGenres(), book.getTags()))
                .thenReturn(List.of(book));
    }

    @Test
    void setupReferenceData_authenticatedUser_loadsShelvesProgressAndReviews() {
        LoginDetails loginDetails = TestUtils.getLoginDetails(login.getEmail(), login.getHandle(), login.isActive());

        ModelMap model = new ModelMap();
        bookHelper.setupReferenceData(loginDetails, book.getId(), model, true, true);

        assertEquals(book, model.get("book"));

        assertEquals(List.of(customShelf), model.get("shelves"));
        assertEquals(defaultShelf, model.get("defaultShelf"));
        assertEquals(List.of(customShelf), model.get("allShelves"));
        assertFalse((Boolean) model.get("shelvesTruncated"));

        assertEquals(List.of(readingProgress), model.get("readingProgresses"));
        assertInstanceOf(Shelf.class, model.get("tmpShelf"));
        assertInstanceOf(ReadingProgress.class, model.get("tmpProgress"));
        assertInstanceOf(BookReviewDto.class, model.get("reviewDto"));

        assertEquals(new PageImpl<>(List.of(review)), model.get("reviews"));
        assertEquals(List.of(book), model.get("relatedBooks"));
    }

    @Test
    void setupReferenceData_unauthenticatedUser_skipsUserSpecificData() {
        ModelMap model = new ModelMap();
        bookHelper.setupReferenceData(null, book.getId(), model, true, true);

        assertFalse(model.containsKey("shelves"));
        assertFalse(model.containsKey("defaultShelf"));
        assertFalse(model.containsKey("allShelves"));
        assertFalse(model.containsKey("shelvesTruncated"));

        assertFalse(model.containsKey("readingProgresses"));
    }

    @Test
    void setupReferenceData_inactiveBook_throwsOrReturnsNotFound() {
        book.setStatus(Status.ARCHIVED);
        ModelMap model = new ModelMap();

        assertThrows(AssertionError.class,
                () -> bookHelper.setupReferenceData(null, book.getId(), model, true, true));
    }

    @Test
    void setupShelfReferenceData_bookInReadShelf_setsReadAsDefaultShelf() {
        ModelMap model = new ModelMap();
        bookHelper.setupShelfReferenceData(login, model, book);

        assertEquals(defaultShelf, model.get("defaultShelf"));
    }

    @Test
    void setupShelfReferenceData_bookInCurrentlyReadingShelf_setsCurrentlyReadingAsDefaultShelf() {
        defaultShelf.setName(Shelf.SHELF_CURRENTLY_READING);

        ModelMap model = new ModelMap();
        bookHelper.setupShelfReferenceData(login, model, book);

        assertEquals(defaultShelf, model.get("defaultShelf"));
    }

    @Test
    void setupShelfReferenceData_bookInWantToReadShelf_setsWantToReadAsDefaultShelf() {
        defaultShelf.setName(Shelf.SHELF_WANT_TO_READ);

        ModelMap model = new ModelMap();
        bookHelper.setupShelfReferenceData(login, model, book);

        assertEquals(defaultShelf, model.get("defaultShelf"));
    }

    @Test
    void setupShelfReferenceData_bookInNoDefaultShelf_setsNullDefaultShelf() {
        defaultShelf.setBooks(null);

        ModelMap model = new ModelMap();
        bookHelper.setupShelfReferenceData(login, model, book);

        assertNull(model.get("defaultShelf"));
    }

    @Test
    void setupShelfReferenceData_separatesCustomShelvesFromDefaultShelves() {
        ModelMap model = new ModelMap();
        bookHelper.setupShelfReferenceData(login, model, book);

        assertFalse(((Collection<?>) model.get("defaultShelves")).contains(customShelf));
    }

    @Test
    void getDefaultShelf_bookInReadAndCurrentlyReading_prefersRead() {
        Shelf currentlyReading = TestUtils.getShelf(login, Shelf.SHELF_CURRENTLY_READING, true);
        currentlyReading.setBooks(Set.of(TestUtils.getShelvedBook(book, login, currentlyReading)));
        currentlyReading.setId(3L);

        when(shelfService.getShelvesForCollection(login.getEmail())).thenReturn(List.of(defaultShelf, currentlyReading, customShelf));

        ModelMap model = new ModelMap();
        bookHelper.setupShelfReferenceData(login, model, book);

        assertEquals(Shelf.SHELF_READ, ((Shelf) model.get("defaultShelf")).getName());
    }

    @Test
    void getDefaultShelf_bookInCurrentlyReadingOnly_returnsCurrentlyReading() {
        Shelf currentlyReading = TestUtils.getShelf(login, Shelf.SHELF_CURRENTLY_READING, true);
        currentlyReading.setBooks(Set.of(TestUtils.getShelvedBook(book, login, currentlyReading)));
        currentlyReading.setId(3L);

        defaultShelf.setBooks(null);

        when(shelfService.getShelvesForCollection(login.getEmail())).thenReturn(List.of(defaultShelf, currentlyReading, customShelf));

        ModelMap model = new ModelMap();
        bookHelper.setupShelfReferenceData(login, model, book);

        assertEquals(Shelf.SHELF_CURRENTLY_READING, ((Shelf) model.get("defaultShelf")).getName());
    }
}
