package com.zedapps.bookshare.service.book;

import com.zedapps.bookshare.entity.book.Author;
import com.zedapps.bookshare.entity.book.Book;
import com.zedapps.bookshare.entity.book.Genre;
import com.zedapps.bookshare.entity.book.Tag;
import com.zedapps.bookshare.entity.login.Login;
import com.zedapps.bookshare.enums.ActivityType;
import com.zedapps.bookshare.enums.Status;
import com.zedapps.bookshare.repository.book.AuthorRepository;
import com.zedapps.bookshare.repository.book.BookRepository;
import com.zedapps.bookshare.repository.book.GenreRepository;
import com.zedapps.bookshare.repository.book.TagRepository;
import com.zedapps.bookshare.service.activity.ActivityService;
import com.zedapps.bookshare.service.auth.LoginDetails;
import com.zedapps.bookshare.util.TestUtils;
import jakarta.persistence.NoResultException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * @author smzoha
 * @since 11/5/26
 **/
@ExtendWith(MockitoExtension.class)
public class BookAdminServiceTest {

    @InjectMocks
    private BookAdminService bookAdminService;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private AuthorRepository authorRepository;

    @Mock
    private GenreRepository genreRepository;

    @Mock
    private TagRepository tagRepository;

    @Mock
    private ActivityService activityService;

    @Captor
    private ArgumentCaptor<Map<String, Object>> activityOutboxPayloadCaptor;

    private Book book;
    private Login login;
    private Author author;
    private Genre genre;
    private Tag tag;

    @BeforeEach
    void setup() {
        login = TestUtils.getLogin("test@test.com", "test", true);
        login.setId(1L);

        author = TestUtils.getAuthor("Test", "Author");
        author.setId(1L);
        author.setLogin(login);

        genre = new Genre();
        genre.setId(1L);
        genre.setName("Genre 1");

        tag = new Tag();
        tag.setId(1L);
        tag.setName("Tag 1");

        book = TestUtils.getBook("Book 1", "9780743273565", author, Status.ACTIVE);
        book.setId(1L);
        book.setGenres(Set.of(genre));
        book.setTags(Set.of(tag));

        LoginDetails loginDetails = TestUtils.getLoginDetails("test@test.com", "test", true);
        TestUtils.setupSecurityContext(loginDetails);

        lenient().when(bookRepository.findBookById(anyLong())).thenReturn(Optional.of(book));

        lenient().when(genreRepository.findById(anyLong())).thenReturn(Optional.of(genre));
        lenient().when(genreRepository.findGenreByName(anyString())).thenReturn(Optional.of(genre));

        lenient().when(tagRepository.findById(anyLong())).thenReturn(Optional.of(tag));
        lenient().when(tagRepository.findTagByName(anyString())).thenReturn(Optional.of(tag));

        lenient().when(authorRepository.findById(anyLong())).thenReturn(Optional.of(author));
        lenient().when(authorRepository.findAuthorByLogin(any(Login.class))).thenReturn(Optional.of(author));

        lenient().when(bookRepository.save(any(Book.class))).thenReturn(book);
        lenient().when(genreRepository.save(any(Genre.class))).thenReturn(genre);
        lenient().when(tagRepository.save(any(Tag.class))).thenReturn(tag);
        lenient().when(authorRepository.save(any(Author.class))).thenReturn(author);
    }

    @AfterEach
    void teardown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getBookList_returnsAllBooks() {
        when(bookRepository.findAll()).thenReturn(List.of(book));

        List<Book> bookList = bookAdminService.getBookList();

        assertNotNull(bookList);
        assertEquals(1, bookList.size());
        assertEquals(book, bookList.getFirst());
    }

    @Test
    void getBook_existingId_returnsBook() {
        Book persistedBook = bookAdminService.getBook(1L);

        assertNotNull(persistedBook);
        assertEquals(book, persistedBook);
    }

    @Test
    void getBook_missingId_throwsNoResultException() {
        when(bookRepository.findBookById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NoResultException.class, () -> bookAdminService.getBook(1L));
    }

    @Test
    void getGenreList_returnsSortedByName() {
        Genre genre2 = new Genre();
        genre2.setId(2L);
        genre2.setName("Alpha Genre");

        when(genreRepository.findAll()).thenReturn(List.of(genre, genre2));

        List<Genre> genreList = bookAdminService.getGenreList();

        assertNotNull(genreList);
        assertEquals(2, genreList.size());
        assertEquals(genre2, genreList.getFirst());
        assertEquals(genre, genreList.get(1));
    }

    @Test
    void getGenre_existingId_returnsGenre() {
        Genre persistedGenre = bookAdminService.getGenre(1L);

        assertNotNull(persistedGenre);
        assertEquals(genre, persistedGenre);
    }

    @Test
    void getGenre_missingId_throwsNoResultException() {
        when(genreRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NoResultException.class, () -> bookAdminService.getGenre(1L));
    }

    @Test
    void getGenreByName_existingName_returnsNonEmptyOptional() {
        Optional<Genre> genreOptional = bookAdminService.getGenreByName("Genre 1");

        assertTrue(genreOptional.isPresent());
        assertEquals(genre, genreOptional.get());
    }

    @Test
    void getGenreByName_missingName_returnsEmptyOptional() {
        when(genreRepository.findGenreByName(anyString())).thenReturn(Optional.empty());

        Optional<Genre> genreOptional = bookAdminService.getGenreByName("Test 2");
        assertTrue(genreOptional.isEmpty());
    }

    @Test
    void getTagList_returnsSortedByName() {
        Tag tag2 = new Tag();
        tag2.setId(2L);
        tag2.setName("Alpha Tag");

        when(tagRepository.findAll()).thenReturn(List.of(tag, tag2));

        List<Tag> tagList = bookAdminService.getTagList();

        assertNotNull(tagList);
        assertEquals(2, tagList.size());
        assertEquals(tag2, tagList.getFirst());
        assertEquals(tag, tagList.get(1));
    }

    @Test
    void getTag_existingId_returnsTag() {
        Tag persistedTag = bookAdminService.getTag(1L);

        assertNotNull(persistedTag);
        assertEquals(tag, persistedTag);
    }

    @Test
    void getTag_missingId_throwsNoResultException() {
        when(tagRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NoResultException.class, () -> bookAdminService.getTag(1L));
    }

    @Test
    void getTagByName_existingName_returnsNonEmptyOptional() {
        Optional<Tag> tagOptional = bookAdminService.getTagByName("Tag 1");

        assertTrue(tagOptional.isPresent());
        assertEquals(tag, tagOptional.get());
    }

    @Test
    void getTagByName_missingName_returnsEmptyOptional() {
        when(tagRepository.findTagByName(anyString())).thenReturn(Optional.empty());

        Optional<Tag> tagOptional = bookAdminService.getTagByName("Test 2");
        assertTrue(tagOptional.isEmpty());
    }

    @Test
    void getAuthorList_returnsAllAuthors() {
        when(authorRepository.findAll()).thenReturn(List.of(author));

        List<Author> authorList = bookAdminService.getAuthorList();

        assertNotNull(authorList);
        assertEquals(1, authorList.size());
        assertEquals(author, authorList.getFirst());
    }

    @Test
    void getAuthor_existingId_returnsAuthor() {
        Author persistedAuthor = bookAdminService.getAuthor(1L);

        assertNotNull(persistedAuthor);
        assertEquals(author, persistedAuthor);
    }

    @Test
    void getAuthor_missingId_throwsNoResultException() {
        when(authorRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NoResultException.class, () -> bookAdminService.getAuthor(1L));
    }

    @Test
    void getAuthorByLogin_existingLogin_returnsNonEmptyOptional() {
        Optional<Author> authorOptional = bookAdminService.getAuthorByLogin(login);

        assertTrue(authorOptional.isPresent());
        assertEquals(author, authorOptional.get());
        assertEquals(login, authorOptional.get().getLogin());
    }

    @Test
    void getAuthorByLogin_missingLogin_returnsEmptyOptional() {
        when(authorRepository.findAuthorByLogin(login)).thenReturn(Optional.empty());

        Optional<Author> authorOptional = bookAdminService.getAuthorByLogin(login);
        assertTrue(authorOptional.isEmpty());
    }

    @Test
    void saveBook_newBook_firesBookAddActivityOutbox() {
        Book newBook = TestUtils.getBook("Book 1", "9780743273565", author, Status.ACTIVE);
        bookAdminService.saveBook(newBook, null);

        verify(activityService).saveActivityOutbox(eq(ActivityType.BOOK_ADD),
                eq(book.getId()), anyMap());
    }

    @Test
    void saveBook_existingBook_firesBookUpdateActivityOutbox() {
        bookAdminService.saveBook(book, null);

        verify(activityService).saveActivityOutbox(eq(ActivityType.BOOK_UPDATE),
                eq(book.getId()), anyMap());
    }

    @Test
    void saveBook_explicitActivityType_usesProvidedType() {
        bookAdminService.saveBook(book, ActivityType.BOOK_REQUEST_SAVE);

        verify(activityService).saveActivityOutbox(eq(ActivityType.BOOK_REQUEST_SAVE),
                eq(book.getId()), anyMap());
    }

    @Test
    void saveBook_newBook_includesCorrectPayloadFields() {
        Book newBook = TestUtils.getBook("Book 1", "9780743273565", author, Status.ACTIVE);
        bookAdminService.saveBook(newBook, null);

        verify(activityService).saveActivityOutbox(eq(ActivityType.BOOK_ADD),
                eq(book.getId()),
                activityOutboxPayloadCaptor.capture());

        assertThat(activityOutboxPayloadCaptor.getValue())
                .containsEntry("actionBy", "test@test.com")
                .containsEntry("affectedBookId", book.getId())
                .containsEntry("bookName", book.getTitle())
                .containsEntry("bookIsbn", book.getIsbn());
    }

    @Test
    void saveGenre_newGenre_firesGenreAddOutbox() {
        Genre newGenre = new Genre();
        newGenre.setName("Genre 1");

        bookAdminService.saveGenre(newGenre);

        verify(activityService).saveActivityOutbox(eq(ActivityType.GENRE_ADD),
                eq(genre.getId()), anyMap());
    }

    @Test
    void saveGenre_existingGenre_firesGenreUpdateOutbox() {
        bookAdminService.saveGenre(genre);

        verify(activityService).saveActivityOutbox(eq(ActivityType.GENRE_UPDATE),
                eq(genre.getId()), anyMap());
    }

    @Test
    void saveGenre_newGenre_includesCorrectPayloadFields() {
        Genre newGenre = new Genre();
        newGenre.setName("Genre 1");
        bookAdminService.saveGenre(newGenre);

        verify(activityService).saveActivityOutbox(eq(ActivityType.GENRE_ADD),
                eq(genre.getId()),
                activityOutboxPayloadCaptor.capture());

        assertThat(activityOutboxPayloadCaptor.getValue())
                .containsEntry("actionBy", "test@test.com")
                .containsEntry("affectedGenreId", genre.getId())
                .containsEntry("genreName", genre.getName());
    }

    @Test
    void saveTag_newTag_firesTagAddOutbox() {
        Tag newTag = new Tag();
        newTag.setName("Tag 1");

        bookAdminService.saveTag(newTag);

        verify(activityService).saveActivityOutbox(eq(ActivityType.TAG_ADD),
                eq(tag.getId()), anyMap());
    }

    @Test
    void saveTag_existingTag_firesTagUpdateOutbox() {
        bookAdminService.saveTag(tag);

        verify(activityService).saveActivityOutbox(eq(ActivityType.TAG_UPDATE),
                eq(tag.getId()), anyMap());
    }

    @Test
    void saveTag_newTag_includesCorrectPayloadFields() {
        Tag newTag = new Tag();
        newTag.setName("Tag 1");
        bookAdminService.saveTag(newTag);

        verify(activityService).saveActivityOutbox(eq(ActivityType.TAG_ADD),
                eq(tag.getId()),
                activityOutboxPayloadCaptor.capture());

        assertThat(activityOutboxPayloadCaptor.getValue())
                .containsEntry("actionBy", "test@test.com")
                .containsEntry("affectedTagId", tag.getId())
                .containsEntry("tagName", tag.getName());
    }

    @Test
    void saveAuthor_newAuthor_firesAuthorAddOutbox() {
        Author newAuthor = TestUtils.getAuthor("Test", "Author");
        bookAdminService.saveAuthor(newAuthor);

        verify(activityService).saveActivityOutbox(eq(ActivityType.AUTHOR_ADD),
                eq(author.getId()), anyMap());
    }

    @Test
    void saveAuthor_existingAuthor_firesAuthorUpdateOutbox() {
        bookAdminService.saveAuthor(author);

        verify(activityService).saveActivityOutbox(eq(ActivityType.AUTHOR_UPDATE),
                eq(author.getId()), anyMap());
    }

    @Test
    void saveAuthor_nullLogin_setsEmptyStringForLoginInOutboxPayload() {
        author.setLogin(null);
        bookAdminService.saveAuthor(author);

        verify(activityService).saveActivityOutbox(eq(ActivityType.AUTHOR_UPDATE),
                eq(author.getId()),
                activityOutboxPayloadCaptor.capture());

        assertThat(activityOutboxPayloadCaptor.getValue())
                .containsEntry("actionBy", "test@test.com")
                .containsEntry("affectedAuthorId", author.getId())
                .containsEntry("affectedAuthorLogin", "");
    }
}
