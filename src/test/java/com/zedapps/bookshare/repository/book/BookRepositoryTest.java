package com.zedapps.bookshare.repository.book;

import com.zedapps.bookshare.entity.book.Author;
import com.zedapps.bookshare.entity.book.Book;
import com.zedapps.bookshare.entity.book.Genre;
import com.zedapps.bookshare.entity.book.Tag;
import com.zedapps.bookshare.entity.login.Login;
import com.zedapps.bookshare.entity.login.Review;
import com.zedapps.bookshare.enums.Status;
import com.zedapps.bookshare.repository.login.LoginRepository;
import com.zedapps.bookshare.repository.login.ReviewRepository;
import com.zedapps.bookshare.util.TestUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author smzoha
 * @since 2/5/26
 **/
@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class BookRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17-alpine");

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private GenreRepository genreRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private LoginRepository loginRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    private Book book1;
    private Book book2;
    private Book book3;
    private Book book4;

    private Genre horrorGenre;
    private Genre actionGenre;

    private Tag historicalTag;
    private Tag oceanTag;

    private Author author1;

    @BeforeAll
    void setUp() {
        author1 = TestUtils.getAuthor("Test", "Author");
        Author author2 = TestUtils.getAuthor("Zest", "Author");
        authorRepository.saveAllAndFlush(List.of(author1, author2));

        Login login = TestUtils.getLogin("test@test.com", "test", true);
        loginRepository.saveAndFlush(login);

        setupGenres();
        setupTags();
        setupBooks(author1, author2);

        bookRepository.saveAllAndFlush(List.of(book1, book2, book3, book4));

        Review review = TestUtils.getReview(book1, login, 3);
        reviewRepository.saveAndFlush(review);
    }

    @Test
    void getFeaturedBooks_returnList() {
        setupAdditionalBooks();

        List<Book> featuredBooks = bookRepository.getFeaturedBooks();

        assertFalse(featuredBooks.isEmpty());
        assertEquals(10, featuredBooks.size());
        assertTrue(featuredBooks.stream().allMatch(b -> b.getStatus() == Status.ACTIVE));

        assertEquals(book1, featuredBooks.getFirst());
    }

    @Test
    void getRelatedBooks_returnList() {
        List<Book> relatedBooks = bookRepository.getRelatedBooks(Set.of(horrorGenre, actionGenre), Set.of(oceanTag));

        assertFalse(relatedBooks.isEmpty());
        assertEquals(3, relatedBooks.size());

        assertEquals(1, relatedBooks.stream().filter(b -> b.getStatus() == Status.PENDING).count());
        assertEquals(2, relatedBooks.stream().filter(b -> b.getStatus() == Status.ACTIVE).count());

        assertEquals(2, relatedBooks.stream().filter(b -> b.getGenres().contains(horrorGenre)).count());
        assertEquals(2, relatedBooks.stream().filter(b -> b.getGenres().contains(actionGenre)).count());

        assertEquals(1, relatedBooks.stream().filter(b -> b.getTags().contains(historicalTag)).count());
        assertEquals(1, relatedBooks.stream().filter(b -> b.getTags().contains(oceanTag)).count());
    }

    @Test
    void getPaginatedBooks_nullFilters_returnPagedBooks() {
        Pageable pageable = PageRequest.of(0, 2);
        Page<Book> paginatedBooks = bookRepository.getPaginatedBooks(pageable, null, null, null, null, null, null);

        assertFalse(paginatedBooks.isEmpty());
        assertEquals(1, paginatedBooks.getTotalPages());
        assertEquals(2, paginatedBooks.getTotalElements());
        assertEquals(0, paginatedBooks.getNumber());
        assertTrue(paginatedBooks.getContent().stream().allMatch(b -> b.getStatus() == Status.ACTIVE));
    }

    @Test
    void getPaginatedBooks_filterQuery_returnPagedBooks() {
        Pageable pageable = PageRequest.of(0, 2);
        Page<Book> paginatedBooks = bookRepository.getPaginatedBooks(pageable, "Book 1".toLowerCase(LocaleContextHolder.getLocale()),
                null, null, null, null, null);

        assertFalse(paginatedBooks.isEmpty());
        assertEquals(1, paginatedBooks.getTotalElements());
        assertEquals(book1, paginatedBooks.getContent().getFirst());

        paginatedBooks = bookRepository.getPaginatedBooks(pageable, "9780061120084", null, null, null, null, null);

        assertFalse(paginatedBooks.isEmpty());
        assertEquals(1, paginatedBooks.getTotalElements());
        assertEquals(book2, paginatedBooks.getContent().getFirst());
    }

    @Test
    void getPaginatedBooks_filterGenre_returnPagedBooks() {
        Pageable pageable = PageRequest.of(0, 2);
        Page<Book> paginatedBooks = bookRepository.getPaginatedBooks(pageable, null, null, String.valueOf(horrorGenre.getId()),
                null, "title", "asc");

        assertFalse(paginatedBooks.isEmpty());
        assertEquals(1, paginatedBooks.getTotalPages());
        assertEquals(2, paginatedBooks.getTotalElements());
        assertEquals(0, paginatedBooks.getNumber());

        assertTrue(paginatedBooks.getContent().stream().allMatch(b -> b.getGenres().contains(horrorGenre)));
        assertEquals(List.of(book1, book2), paginatedBooks.getContent());
    }

    @Test
    void getPaginatedBooks_filterTag_returnPagedBooks() {
        Pageable pageable = PageRequest.of(0, 2);
        Page<Book> paginatedBooks = bookRepository.getPaginatedBooks(pageable, null, null, null,
                String.valueOf(oceanTag.getId()), null, null);

        assertFalse(paginatedBooks.isEmpty());
        assertEquals(1, paginatedBooks.getTotalPages());
        assertEquals(1, paginatedBooks.getTotalElements());
        assertEquals(0, paginatedBooks.getNumber());

        assertTrue(paginatedBooks.getContent().stream().allMatch(b -> b.getTags().contains(oceanTag)));
        assertEquals(book1, paginatedBooks.getContent().getFirst());
    }

    @Test
    void getPaginatedBooks_filterRatings_returnPagedBooks() {
        Pageable pageable = PageRequest.of(0, 2);
        Page<Book> paginatedBooks = bookRepository.getPaginatedBooks(pageable, null, "4", null,
                null, null, null);

        assertTrue(paginatedBooks.isEmpty());

        paginatedBooks = bookRepository.getPaginatedBooks(pageable, null, "3", null,
                null, null, null);

        assertFalse(paginatedBooks.isEmpty());
        assertEquals(1, paginatedBooks.getTotalPages());
        assertEquals(1, paginatedBooks.getTotalElements());
    }

    @Test
    void getPaginatedBooks_sortTitle_returnPagedBooks() {
        Pageable pageable = PageRequest.of(0, 2);

        Page<Book> paginatedBooks = bookRepository.getPaginatedBooks(pageable, null, null, null, null,
                "title", "asc");

        assertFalse(paginatedBooks.isEmpty());

        List<Book> books = paginatedBooks.getContent();
        assertTrue(books.getFirst().getTitle().compareTo(books.getLast().getTitle()) < 0);

        paginatedBooks = bookRepository.getPaginatedBooks(pageable, null, null, null, null,
                "title", "desc");

        assertFalse(paginatedBooks.isEmpty());

        books = paginatedBooks.getContent();
        assertTrue(books.getFirst().getTitle().compareTo(books.getLast().getTitle()) > 0);
    }

    @Test
    void getPaginatedBooks_sortAuthor_returnPagedBooks() {
        Pageable pageable = PageRequest.of(0, 2);

        Page<Book> paginatedBooks = bookRepository.getPaginatedBooks(pageable, null, null, null, null,
                "author", "asc");

        assertFalse(paginatedBooks.isEmpty());

        List<Book> books = paginatedBooks.getContent();
        assertEquals(book1, books.getFirst());
        assertEquals(book2, books.getLast());

        paginatedBooks = bookRepository.getPaginatedBooks(pageable, null, null, null, null,
                "author", "desc");

        assertFalse(paginatedBooks.isEmpty());

        books = paginatedBooks.getContent();
        assertEquals(book2, books.getFirst());
        assertEquals(book1, books.getLast());
    }

    @Test
    void getPaginatedBooks_sortRating_returnPagedBooks() {
        Pageable pageable = PageRequest.of(0, 2);

        Page<Book> paginatedBooks = bookRepository.getPaginatedBooks(pageable, null, null, null, null,
                "rating", "asc");

        assertFalse(paginatedBooks.isEmpty());

        List<Book> books = paginatedBooks.getContent();
        assertEquals(book2, books.getFirst());
        assertEquals(book1, books.getLast());

        paginatedBooks = bookRepository.getPaginatedBooks(pageable, null, null, null, null,
                "rating", "desc");

        assertFalse(paginatedBooks.isEmpty());

        books = paginatedBooks.getContent();
        assertEquals(book1, books.getFirst());
        assertEquals(book2, books.getLast());
    }

    private void setupBooks(Author... authors) {
        book1 = TestUtils.getBook("Book 1", "9780743273565", authors[0], Status.ACTIVE);
        book1.setGenres(Set.of(horrorGenre));
        book1.setTags(Set.of(oceanTag));

        book2 = TestUtils.getBook("Book 2", "9780061120084", authors[1], Status.ACTIVE);
        book2.setGenres(Set.of(horrorGenre, actionGenre));
        book2.setTags(Set.of(historicalTag));

        book3 = TestUtils.getBook("Book 3", "9780743273572", authors[0], Status.PENDING);
        book3.setGenres(Set.of(actionGenre));

        book4 = TestUtils.getBook("Book 4", "9780140283297", authors[0], Status.ARCHIVED);
    }

    private void setupGenres() {
        horrorGenre = TestUtils.getGenre("Horror");
        actionGenre = TestUtils.getGenre("Action");

        genreRepository.saveAllAndFlush(List.of(horrorGenre, actionGenre));
    }

    private void setupTags() {
        historicalTag = TestUtils.getTag("Historical");
        oceanTag = TestUtils.getTag("Ocean");

        tagRepository.saveAllAndFlush(List.of(historicalTag, oceanTag));
    }

    private void setupAdditionalBooks() {
        List<String> isbns = List.of(
                "9780132350884",
                "9780134685991",
                "9780135166307",
                "9780596517748",
                "9780596009205",
                "9780201633610",
                "9780321125217",
                "9780321200686",
                "9780321534965",
                "9780743477123"
        );

        List<Book> additionalBooks = new ArrayList<>();

        for (int i = 0; i < isbns.size(); i++) {
            Book book = TestUtils.getBook("Book " + (100 + i), isbns.get(i), author1, Status.ACTIVE);
            additionalBooks.add(book);
        }

        bookRepository.saveAllAndFlush(additionalBooks);
    }
}
