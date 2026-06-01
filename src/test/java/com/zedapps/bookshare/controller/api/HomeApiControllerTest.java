package com.zedapps.bookshare.controller.api;

import com.zedapps.bookshare.controller.AbstractWebMvcTest;
import com.zedapps.bookshare.dto.api.book.BookDto;
import com.zedapps.bookshare.dto.api.feed.FeedApiResponse;
import com.zedapps.bookshare.entity.book.Author;
import com.zedapps.bookshare.entity.book.Book;
import com.zedapps.bookshare.entity.login.Login;
import com.zedapps.bookshare.enums.Status;
import com.zedapps.bookshare.repository.book.BookRepository;
import com.zedapps.bookshare.security.WithMockLoginDetails;
import com.zedapps.bookshare.service.book.BookApiService;
import com.zedapps.bookshare.service.login.FeedApiService;
import com.zedapps.bookshare.service.login.LoginService;
import com.zedapps.bookshare.util.TestUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author smzoha
 * @since 26/5/26
 **/
@WebMvcTest(HomeApiController.class)
public class HomeApiControllerTest extends AbstractWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookRepository bookRepository;

    @MockitoBean
    private BookApiService bookApiService;

    @MockitoBean
    private FeedApiService feedApiService;

    @MockitoBean
    private LoginService loginService;

    @Test
    void getFeaturedBooks_always_returns200WithBookDtoList() throws Exception {
        List<Book> bookList = getBookList();
        BookDto bookDto = new BookDto("Book", "0134685997", null, null, null, null,
                null, List.of(), List.of(), List.of(), null);

        when(bookRepository.getFeaturedBooks()).thenReturn(bookList);
        when(bookApiService.createDto(any(Book.class), eq(false))).thenReturn(bookDto);

        mockMvc.perform(get("/api/v1/home/featured"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(bookList.size()));
    }

    @Test
    @WithMockLoginDetails
    void getFeed_authenticatedUser_returns200WithFeedApiResponse() throws Exception {
        Login login = TestUtils.getLogin("test@test.com", "test", true);
        FeedApiResponse feedApiResponse = new FeedApiResponse(List.of(), 0, 1);

        when(loginService.getLogin("test@test.com")).thenReturn(login);
        when(feedApiService.getFeedApiResponse(login, 5, 0)).thenReturn(feedApiResponse);

        mockMvc.perform(get("/api/v1/home/feed"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.feedEntries").isArray())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    @WithMockLoginDetails
    void getFeed_defaultPage_usesPageZero() throws Exception {
        Login login = TestUtils.getLogin("test@test.com", "test", true);

        when(loginService.getLogin("test@test.com")).thenReturn(login);
        when(feedApiService.getFeedApiResponse(any(), eq(5), eq(0))).thenReturn(new FeedApiResponse(List.of(), 0, 0));

        mockMvc.perform(get("/api/v1/home/feed"))
                .andExpect(status().isOk());

        verify(feedApiService).getFeedApiResponse(login, 5, 0);
    }

    @Test
    @WithMockLoginDetails
    void getFeed_customPage_usesProvidedPage() throws Exception {
        Login login = TestUtils.getLogin("test@test.com", "test", true);

        when(loginService.getLogin("test@test.com")).thenReturn(login);
        when(feedApiService.getFeedApiResponse(any(), eq(5), eq(2))).thenReturn(new FeedApiResponse(List.of(), 2, 5));

        mockMvc.perform(get("/api/v1/home/feed").param("page", "2"))
                .andExpect(status().isOk());

        verify(feedApiService).getFeedApiResponse(login, 5, 2);
    }

    private List<Book> getBookList() {
        List<Book> bookList = new ArrayList<>();

        Author author = TestUtils.getAuthor("Test", "Author");
        author.setId(1L);

        for (int i = 0; i < TestUtils.TEST_ISBN_DATA.size(); i++) {
            String isbn = TestUtils.TEST_ISBN_DATA.get(i);

            Book book = TestUtils.getBook("Book " + i, isbn, author, Status.ACTIVE);
            book.setId((long) (i + 1));

            bookList.add(book);
        }

        return bookList;
    }
}
