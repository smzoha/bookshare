package com.zedapps.bookshare.controller.book.admin;

import com.zedapps.bookshare.controller.AbstractWebMvcTest;
import com.zedapps.bookshare.dto.activity.ActivityEvent;
import com.zedapps.bookshare.entity.book.Author;
import com.zedapps.bookshare.entity.book.AuthorRequest;
import com.zedapps.bookshare.entity.login.Login;
import com.zedapps.bookshare.enums.Role;
import com.zedapps.bookshare.repository.book.AuthorRequestRepository;
import com.zedapps.bookshare.security.WithMockLoginDetails;
import com.zedapps.bookshare.service.book.BookAdminService;
import com.zedapps.bookshare.service.login.LoginService;
import com.zedapps.bookshare.util.TestUtils;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * @author smzoha
 * @since 31/5/26
 **/
@WebMvcTest(AuthorAdminController.class)
@WithMockLoginDetails(role = "ADMIN")
@RecordApplicationEvents
public class AuthorAdminControllerTest extends AbstractWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookAdminService bookAdminService;

    @MockitoBean
    private LoginService loginService;

    @MockitoBean
    private AuthorRequestRepository authorRequestRepository;

    @Autowired
    private ApplicationEvents applicationEvents;

    private List<Author> authors;
    private Author author;
    private Login login;

    @BeforeEach
    void setUp() {
        authors = new ArrayList<>();

        for (int i = 1; i < 10; i++) {
            Author author = TestUtils.getAuthor("Author", String.valueOf(i));
            author.setId((long) i);

            authors.add(author);
        }

        login = TestUtils.getLogin("author@test.com", "author", true);
        login.setId(1L);

        author = TestUtils.getAuthor("Test", "Author");
        author.setId(100L);
        author.setLogin(login);
        authors.add(author);
    }

    @Test
    void listAuthors_always_returnsAuthorListView() throws Exception {
        when(bookAdminService.getAuthorList()).thenReturn(authors);

        mockMvc.perform(get("/admin/author/list"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("authors", authors))
                .andExpect(view().name("admin/author/authorList"));

        verify(bookAdminService).getAuthorList();
        assertThat(applicationEvents.stream(ActivityEvent.class)).hasSize(1);
    }

    @Test
    void showNewAuthorForm_always_returnsEmptyAuthorForm() throws Exception {
        mockMvc.perform(get("/admin/author/new"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("author"))
                .andExpect(model().attribute("author", hasProperty("id", equalTo(null))))
                .andExpect(model().attribute("author", hasProperty("firstName", equalTo(null))))
                .andExpect(model().attribute("author", hasProperty("lastName", equalTo(null))))
                .andExpect(model().attribute("author", hasProperty("login", equalTo(null))))
                .andExpect(view().name("admin/author/authorForm"));
    }

    @Test
    void showEditAuthorForm_existingId_returnsPopulatedAuthorForm() throws Exception {
        when(bookAdminService.getAuthor(author.getId())).thenReturn(author);

        mockMvc.perform(get("/admin/author/" + author.getId()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("author"))
                .andExpect(model().attribute("author", hasProperty("id", equalTo(author.getId()))))
                .andExpect(model().attribute("author", hasProperty("firstName", equalTo(author.getFirstName()))))
                .andExpect(model().attribute("author", hasProperty("lastName", equalTo(author.getLastName()))))
                .andExpect(model().attribute("author", hasProperty("login", equalTo(author.getLogin()))))
                .andExpect(view().name("admin/author/authorForm"));

        verify(bookAdminService).getAuthor(author.getId());
        assertThat(applicationEvents.stream(ActivityEvent.class)).hasSize(1);
    }

    @Test
    void saveAuthor_validAuthor_savesAndRedirects() throws Exception {
        mockMvc.perform(post("/admin/author/save")
                        .param("firstName", author.getFirstName())
                        .param("lastName", author.getLastName()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin"));

        verify(bookAdminService).saveAuthor(any(Author.class));
    }

    @Test
    void saveAuthor_duplicateLoginLink_returnsFormWithError() throws Exception {
        when(bookAdminService.getAuthorByLogin(login)).thenReturn(Optional.of(author));

        mockMvc.perform(post("/admin/author/save")
                        .param("firstName", author.getFirstName())
                        .param("lastName", author.getLastName())
                        .param("login.id", String.valueOf(author.getLogin().getId())))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrorCode("author", "login", "error.input.exists"))
                .andExpect(view().name("admin/author/authorForm"));

        verify(bookAdminService, never()).saveAuthor(any(Author.class));
    }

    @Test
    void listAuthorRequests_always_returnsPendingRequestsView() throws Exception {
        AuthorRequest authorRequest = new AuthorRequest();
        authorRequest.setLogin(login);
        authorRequest.setId(1L);

        when(authorRequestRepository.findAll()).thenReturn(List.of(authorRequest));

        mockMvc.perform(get("/admin/author/request"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("requestList", List.of(authorRequest)))
                .andExpect(view().name("admin/author/requestList"));

        verify(authorRequestRepository).findAll();
    }

    @Test
    void processAuthorRequest_validRequest_createsAuthorUpgradesRoleDeletesRequest() throws Exception {
        AuthorRequest authorRequest = new AuthorRequest();
        authorRequest.setLogin(login);
        authorRequest.setId(1L);

        ArgumentCaptor<Author> authorCaptor = ArgumentCaptor.forClass(Author.class);
        ArgumentCaptor<Login> loginCaptor = ArgumentCaptor.forClass(Login.class);

        when(authorRequestRepository.findById(1L)).thenReturn(Optional.of(authorRequest));
        when(loginService.getLogin(login.getEmail())).thenReturn(login);

        mockMvc.perform(post("/admin/author/request/process")
                        .param("id", authorRequest.getId().toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/author/request"));

        verify(bookAdminService).saveAuthor(authorCaptor.capture());
        verify(loginService).saveLogin(loginCaptor.capture());

        assertEquals(login, authorCaptor.getValue().getLogin());
        assertEquals(Role.AUTHOR, loginCaptor.getValue().getRole());

        verify(authorRequestRepository).delete(authorRequest);
    }

    @Test
    void processAuthorRequest_requestNotFound_throwsServletException() {
        when(authorRequestRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ServletException.class,
                () -> mockMvc.perform(post("/admin/author/request/process")
                        .param("id", "1")));
    }

    @Test
    void getAuthor_authorWithoutLoginLink_metadataUsesEmptyAffectedAuthorLogin() throws Exception {
        author.setLogin(null);
        when(bookAdminService.getAuthor(author.getId())).thenReturn(author);

        mockMvc.perform(get("/admin/author/" + author.getId()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("author"))
                .andExpect(view().name("admin/author/authorForm"));

        verify(bookAdminService).getAuthor(author.getId());

        ActivityEvent event = applicationEvents.stream(ActivityEvent.class).findFirst().orElseThrow();
        assertEquals("", event.metadata().get("affectedAuthorLogin"));
    }
}
