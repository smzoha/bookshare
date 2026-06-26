package com.zedapps.bookshare.controller;

import com.zedapps.bookshare.entity.book.Author;
import com.zedapps.bookshare.entity.book.Book;
import com.zedapps.bookshare.entity.book.Genre;
import com.zedapps.bookshare.entity.book.Tag;
import com.zedapps.bookshare.entity.login.Login;
import com.zedapps.bookshare.entity.login.ReadingProgress;
import com.zedapps.bookshare.repository.book.BookRepository;
import com.zedapps.bookshare.repository.book.GenreRepository;
import com.zedapps.bookshare.security.WithMockLoginDetails;
import com.zedapps.bookshare.service.login.FeedService;
import com.zedapps.bookshare.service.login.LoginService;
import com.zedapps.bookshare.service.login.ProfileService;
import com.zedapps.bookshare.util.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * @author smzoha
 * @since 7/6/26
 **/
@WebMvcTest(HomeController.class)
public class HomeControllerTest extends AbstractWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LoginService loginService;

    @MockitoBean
    private ProfileService profileService;

    @MockitoBean
    private BookRepository bookRepository;

    @MockitoBean
    private GenreRepository genreRepository;

    @MockitoBean
    private FeedService feedService;

    private List<Book> bookList;
    private Genre genre;
    private Login login;
    private ReadingProgress readingProgress;

    @BeforeEach
    void setUp() {
        Author author = TestUtils.getAuthor("Test", "Author");
        author.setId(1L);

        login = TestUtils.getLogin("test@test.com", "test", true);
        login.setId(1L);

        genre = TestUtils.getGenre("Genre");
        genre.setId(1L);

        Tag tag = TestUtils.getTag("Tag");
        tag.setId(1L);

        bookList = TestUtils.getBooks(author, Set.of(genre), Set.of(tag));

        readingProgress = TestUtils.getReadingProgress(bookList.getFirst(), login, 10L, LocalDate.now(), null, false);
        readingProgress.setId(1L);
        readingProgress.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    @WithAnonymousUser
    void getHome_unauthenticatedUser_featuredBooksAndGenresInModel() throws Exception {
        when(bookRepository.getFeaturedBooks()).thenReturn(bookList.subList(1, bookList.size()));
        when(genreRepository.findAll()).thenReturn(List.of(genre));

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("featuredBooks", bookList.subList(1, bookList.size())))
                .andExpect(model().attribute("genres", List.of(genre)))
                .andExpect(model().attributeDoesNotExist("login", "readingProgressList"))
                .andExpect(view().name("home"));

        verify(bookRepository).getFeaturedBooks();
        verify(genreRepository).findAll();

        verify(loginService, never()).getLogin(login.getEmail());
        verify(profileService, never()).getDistinctReadingProgressList(login);
    }

    @Test
    @WithMockLoginDetails
    void getHome_authenticatedUser_loginAndReadingProgressListInModel() throws Exception {
        when(bookRepository.getFeaturedBooks()).thenReturn(bookList.subList(1, bookList.size()));
        when(genreRepository.findAll()).thenReturn(List.of(genre));

        when(loginService.getLogin(login.getEmail())).thenReturn(login);
        when(profileService.getDistinctReadingProgressList(login)).thenReturn(List.of(readingProgress));

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("featuredBooks", bookList.subList(1, bookList.size())))
                .andExpect(model().attribute("genres", List.of(genre)))
                .andExpect(model().attribute("login", login))
                .andExpect(model().attribute("readingProgressList", List.of(readingProgress)))
                .andExpect(view().name("home"));

        verify(bookRepository).getFeaturedBooks();
        verify(genreRepository).findAll();

        verify(loginService).getLogin(login.getEmail());
        verify(profileService).getDistinctReadingProgressList(login);
    }

    @Test
    @WithAnonymousUser
    void getAdminHome_unauthenticatedUser_redirectToLogin() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithAnonymousUser
    void getFeed_unauthenticatedUser_redirectToLogin() throws Exception {
        mockMvc.perform(get("/feed"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));

        verifyNoInteractions(loginService, feedService);
    }

    @Test
    @WithMockLoginDetails
    @SuppressWarnings("unchecked")
    void getFeed_authenticatedUser_returnFeedFragment() throws Exception {
        when(loginService.getLogin(login.getEmail())).thenReturn(login);
        when(feedService.getFeedEntries(login, 5, 0)).thenReturn(new PageImpl<>(List.of()));
        when(feedService.mapToFeedDtoList(any(PageImpl.class))).thenReturn(List.of());

        mockMvc.perform(get("/feed"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("feedDtoList", List.of()))
                .andExpect(model().attribute("currentPage", 0))
                .andExpect(model().attribute("totalPages", 0))
                .andExpect(view().name("app/userFeedFragment :: userFeed"));

        verify(loginService).getLogin(login.getEmail());
        verify(feedService).getFeedEntries(login, 5, 0);
        verify(feedService).mapToFeedDtoList(any(PageImpl.class));
    }

    @Test
    @WithMockLoginDetails
    void getAdminHome_withUserRole_returnForbiddenStatus() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockLoginDetails(role = "ADMIN")
    void getAdminHome_withAdminRole_returnAdminHomeView() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().isOk())
                .andExpect(view().name("adminHome"));
    }
}
