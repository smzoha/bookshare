package com.zedapps.bookshare.controller.book.app;

import com.zedapps.bookshare.controller.AbstractWebMvcTest;
import com.zedapps.bookshare.entity.book.Author;
import com.zedapps.bookshare.entity.book.Book;
import com.zedapps.bookshare.entity.book.Genre;
import com.zedapps.bookshare.entity.book.Tag;
import com.zedapps.bookshare.entity.login.Login;
import com.zedapps.bookshare.enums.ActivityType;
import com.zedapps.bookshare.enums.Role;
import com.zedapps.bookshare.enums.Status;
import com.zedapps.bookshare.repository.book.AuthorRepository;
import com.zedapps.bookshare.repository.book.GenreRepository;
import com.zedapps.bookshare.repository.book.TagRepository;
import com.zedapps.bookshare.repository.image.ImageRepository;
import com.zedapps.bookshare.security.WithMockLoginDetails;
import com.zedapps.bookshare.service.book.BookAdminService;
import com.zedapps.bookshare.service.login.LoginService;
import com.zedapps.bookshare.util.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
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
@WebMvcTest(BookRequestController.class)
@WithMockLoginDetails(role = "AUTHOR")
public class BookRequestControllerTest extends AbstractWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LoginService loginService;

    @MockitoBean
    private AuthorRepository authorRepository;

    @MockitoBean
    private GenreRepository genreRepository;

    @MockitoBean
    private TagRepository tagRepository;

    @MockitoBean
    private ImageRepository imageRepository;

    @MockitoBean
    private BookAdminService bookAdminService;

    @MockitoSpyBean
    private BookRequestController bookRequestController;

    private Login login;
    private Author author;

    @BeforeEach
    void setUp() {
        login = TestUtils.getLogin("test@test.com", "test", true);
        login.setRole(Role.AUTHOR);
        login.setId(1L);

        author = TestUtils.getAuthor("Test", "Author");
        author.setId(1L);

        Genre genre = TestUtils.getGenre("Genre");
        genre.setId(1L);

        Tag tag = TestUtils.getTag("Tag");
        tag.setId(1L);

        when(bookRequestController.getStatusList()).thenReturn(new Status[]{Status.PENDING});
        when(authorRepository.findAll()).thenReturn(Collections.singletonList(author));
        when(genreRepository.findAll()).thenReturn(Collections.singletonList(genre));
        when(tagRepository.findAll()).thenReturn(Collections.singletonList(tag));

        when(loginService.getLogin(login.getEmail())).thenReturn(login);
        when(authorRepository.findAuthorByLogin(login)).thenReturn(Optional.of(author));
    }

    @Test
    void showBookRequestForm_authorUser_returnsFormWithAuthorPrePopulated() throws Exception {
        mockMvc.perform(get("/author/bookRequest"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("book"))
                .andExpect(model().attribute("book",
                        hasProperty("authors", hasItem(author))))
                .andExpect(view().name("common/bookForm"));

        verify(loginService).getLogin(login.getEmail());
        verify(authorRepository).findAuthorByLogin(login);
    }

    @Test
    void submitBookRequest_validBook_savesAndRedirects() throws Exception {
        mockMvc.perform(post("/author/bookRequest")
                        .param("title", "Book 1")
                        .param("isbn", "9780743273565")
                        .param("pages", "300")
                        .param("status", "PENDING"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        verify(bookAdminService).saveBook(any(Book.class), eq(ActivityType.BOOK_REQUEST_SAVE));
    }

    @Test
    void submitBookRequest_validationErrors_returnsFormView() throws Exception {
        mockMvc.perform(post("/author/bookRequest")
                        .param("title", "Book 1")
                        .param("pages", "300")
                        .param("status", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(view().name("common/bookForm"))
                .andExpect(model().attributeHasErrors("book"));

        verify(bookAdminService, never()).saveBook(any(Book.class), eq(ActivityType.BOOK_REQUEST_SAVE));
    }

    @Test
    void submitBookRequest_nullImage_savesWithNullImage() throws Exception {
        ArgumentCaptor<Book> bookCaptor = ArgumentCaptor.forClass(Book.class);

        mockMvc.perform(post("/author/bookRequest")
                        .param("title", "Book 1")
                        .param("isbn", "9780743273565")
                        .param("pages", "300")
                        .param("status", "PENDING"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        verify(bookAdminService).saveBook(bookCaptor.capture(), eq(ActivityType.BOOK_REQUEST_SAVE));
        assertNull(bookCaptor.getValue().getImage());
    }
}
