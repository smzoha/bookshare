package com.zedapps.bookshare.controller.book.admin;

import com.zedapps.bookshare.controller.AbstractWebMvcTest;
import com.zedapps.bookshare.dto.activity.ActivityEvent;
import com.zedapps.bookshare.entity.book.Author;
import com.zedapps.bookshare.entity.book.Book;
import com.zedapps.bookshare.entity.book.Genre;
import com.zedapps.bookshare.entity.book.Tag;
import com.zedapps.bookshare.enums.Status;
import com.zedapps.bookshare.repository.book.AuthorRepository;
import com.zedapps.bookshare.repository.book.GenreRepository;
import com.zedapps.bookshare.repository.book.TagRepository;
import com.zedapps.bookshare.repository.image.ImageRepository;
import com.zedapps.bookshare.security.WithMockLoginDetails;
import com.zedapps.bookshare.service.book.BookAdminService;
import com.zedapps.bookshare.util.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * @author smzoha
 * @since 31/5/26
 **/
@WebMvcTest(BookAdminController.class)
@WithMockLoginDetails(role = "ADMIN")
@RecordApplicationEvents
public class BookAdminControllerTest extends AbstractWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookAdminService bookAdminService;

    @MockitoBean
    private AuthorRepository authorRepository;

    @MockitoBean
    private GenreRepository genreRepository;

    @MockitoBean
    private TagRepository tagRepository;

    @MockitoBean
    private ImageRepository imageRepository;

    @Autowired
    private ApplicationEvents applicationEvents;

    private Book book;
    private Author author;
    private Genre genre;
    private Tag tag;

    @BeforeEach
    void setUp() {
        author = TestUtils.getAuthor("Test", "book");
        author.setId(1L);

        genre = TestUtils.getGenre("Genre");
        genre.setId(1L);

        tag = TestUtils.getTag("Tag");
        tag.setId(1L);

        book = TestUtils.getBook("Test Book", "9780451524935", author, Status.ACTIVE);
        book.setId(1L);
        book.setGenres(Set.of(genre));
        book.setTags(Set.of(tag));

        when(authorRepository.findAll()).thenReturn(Collections.singletonList(author));
        when(genreRepository.findAll()).thenReturn(Collections.singletonList(genre));
        when(tagRepository.findAll()).thenReturn(Collections.singletonList(tag));
    }

    @Test
    void listBooks_always_returnsBookListView() throws Exception {
        List<Book> bookList = TestUtils.getBooks(author, Set.of(genre), Set.of(tag));
        when(bookAdminService.getBookList()).thenReturn(bookList);

        mockMvc.perform(get("/admin/book"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("books", bookList))
                .andExpect(view().name("admin/book/bookList"));

        verify(bookAdminService).getBookList();
        assertThat(applicationEvents.stream(ActivityEvent.class)).hasSize(1);
    }

    @Test
    void showNewBookForm_always_returnsEmptyBookForm() throws Exception {
        mockMvc.perform(get("/admin/book/new"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("book"))
                .andExpect(model().attribute("book", hasProperty("id", equalTo(null))))
                .andExpect(model().attribute("book", hasProperty("title", equalTo(null))))
                .andExpect(model().attribute("book", hasProperty("isbn", equalTo(null))))
                .andExpect(model().attribute("book", hasProperty("authors", empty())))
                .andExpect(model().attribute("book", hasProperty("genres", empty())))
                .andExpect(model().attribute("book", hasProperty("tags", empty())))
                .andExpect(view().name("common/bookForm"));
    }

    @Test
    void showEditBookForm_existingId_returnsPopulatedBookForm() throws Exception {
        when(bookAdminService.getBook(book.getId())).thenReturn(book);

        mockMvc.perform(get("/admin/book/" + book.getId()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("book"))
                .andExpect(model().attribute("book", hasProperty("id", equalTo(book.getId()))))
                .andExpect(model().attribute("book", hasProperty("title", equalTo(book.getTitle()))))
                .andExpect(model().attribute("book", hasProperty("isbn", equalTo(book.getIsbn()))))
                .andExpect(model().attribute("book", hasProperty("authors", equalTo(book.getAuthors()))))
                .andExpect(model().attribute("book", hasProperty("genres", equalTo(book.getGenres()))))
                .andExpect(model().attribute("book", hasProperty("tags", equalTo(book.getTags()))))
                .andExpect(view().name("common/bookForm"));

        verify(bookAdminService).getBook(book.getId());
    }

    @Test
    void saveBook_validBook_savesAndRedirects() throws Exception {
        mockMvc.perform(post("/admin/book/save")
                        .param("title", book.getTitle())
                        .param("isbn", book.getIsbn())
                        .param("pages", book.getPages().toString())
                        .param("status", book.getStatus().toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin"));

        verify(bookAdminService).saveBook(any(Book.class), eq(null));
    }

    @Test
    void saveBook_validationErrors_returnsFormView() throws Exception {
        mockMvc.perform(post("/admin/book/save")
                        .param("title", "")
                        .param("isbn", book.getIsbn())
                        .param("pages", book.getPages().toString())
                        .param("status", book.getStatus().toString()))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("book", "title"))
                .andExpect(view().name("common/bookForm"));

        verify(bookAdminService, never()).saveBook(any(Book.class), eq(null));
    }

    @Test
    void saveBook_noImageSelected_savesWithNullImage() throws Exception {
        ArgumentCaptor<Book> captor = ArgumentCaptor.forClass(Book.class);

        mockMvc.perform(post("/admin/book/save")
                        .param("title", book.getTitle())
                        .param("isbn", book.getIsbn())
                        .param("pages", book.getPages().toString())
                        .param("status", book.getStatus().toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin"));

        verify(bookAdminService).saveBook(captor.capture(), eq(null));
        assertNull(captor.getValue().getImage());
    }
}
