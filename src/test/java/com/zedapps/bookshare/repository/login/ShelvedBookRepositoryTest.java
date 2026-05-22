package com.zedapps.bookshare.repository.login;

import com.zedapps.bookshare.entity.book.Author;
import com.zedapps.bookshare.entity.book.Book;
import com.zedapps.bookshare.entity.login.Login;
import com.zedapps.bookshare.entity.login.Shelf;
import com.zedapps.bookshare.entity.login.ShelvedBook;
import com.zedapps.bookshare.enums.Status;
import com.zedapps.bookshare.repository.book.AuthorRepository;
import com.zedapps.bookshare.repository.book.BookRepository;
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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author smzoha
 * @since 27/4/26
 **/
@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class ShelvedBookRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17-alpine");

    @Autowired
    private ShelvedBookRepository shelvedBookRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private LoginRepository loginRepository;

    @Autowired
    private ShelfRepository shelfRepository;

    private Login login;
    private Shelf shelf;

    private Book book;
    private Book unshelvedBook;

    @BeforeEach
    void setUp() {
        login = TestUtils.getLogin("user@test.com", "user", true);
        shelf = TestUtils.getShelf(login, "Test Shelf", false);

        Author author = TestUtils.getAuthor("Test", "Author");

        book = TestUtils.getBook("Test Book", "9780451524935", author, Status.ACTIVE);
        unshelvedBook = TestUtils.getBook("Second Book", "9780679783411", author, Status.ACTIVE);

        loginRepository.saveAndFlush(login);
        shelfRepository.saveAndFlush(shelf);

        authorRepository.saveAndFlush(author);
        bookRepository.saveAndFlush(book);
        bookRepository.saveAndFlush(unshelvedBook);

        ShelvedBook shelvedBook = TestUtils.getShelvedBook(book, login, shelf);
        shelvedBookRepository.saveAndFlush(shelvedBook);
    }

    @Test
    void findShelvedBookByLoginAndShelfAndBook_returnShelvedBook() {
        Optional<ShelvedBook> shelvedBookOptional = shelvedBookRepository.findShelvedBookByLoginAndShelfAndBook(
                login, shelf, book);

        assertTrue(shelvedBookOptional.isPresent());
        assertEquals(login, shelvedBookOptional.get().getLogin());
        assertEquals(book, shelvedBookOptional.get().getBook());
        assertEquals(shelf, shelvedBookOptional.get().getShelf());

        Optional<ShelvedBook> unshelvedBookOptional = shelvedBookRepository.findShelvedBookByLoginAndShelfAndBook(
                login, shelf, unshelvedBook);

        assertTrue(unshelvedBookOptional.isEmpty());
    }
}
