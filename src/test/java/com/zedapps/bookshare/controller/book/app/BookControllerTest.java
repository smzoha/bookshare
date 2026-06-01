package com.zedapps.bookshare.controller.book.app;

import com.zedapps.bookshare.controller.AbstractWebMvcTest;
import com.zedapps.bookshare.dto.activity.ActivityEvent;
import com.zedapps.bookshare.dto.book.BookReviewDto;
import com.zedapps.bookshare.dto.book.ReviewLikeResponseDto;
import com.zedapps.bookshare.entity.book.Author;
import com.zedapps.bookshare.entity.book.Book;
import com.zedapps.bookshare.entity.book.Genre;
import com.zedapps.bookshare.entity.book.Tag;
import com.zedapps.bookshare.entity.login.Login;
import com.zedapps.bookshare.entity.login.ReadingProgress;
import com.zedapps.bookshare.entity.login.Review;
import com.zedapps.bookshare.entity.login.Shelf;
import com.zedapps.bookshare.helper.BookHelper;
import com.zedapps.bookshare.repository.book.GenreRepository;
import com.zedapps.bookshare.repository.book.TagRepository;
import com.zedapps.bookshare.security.WithMockLoginDetails;
import com.zedapps.bookshare.service.auth.LoginDetails;
import com.zedapps.bookshare.service.book.BookService;
import com.zedapps.bookshare.util.TestUtils;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.ui.ModelMap;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * @author smzoha
 * @since 30/5/26
 **/
@WebMvcTest(BookController.class)
@WithMockLoginDetails
@RecordApplicationEvents
public class BookControllerTest extends AbstractWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookHelper bookHelper;

    @MockitoBean
    private BookService bookService;

    @MockitoBean
    private GenreRepository genreRepository;

    @MockitoBean
    private TagRepository tagRepository;

    @Autowired
    private ApplicationEvents applicationEvents;

    private List<Book> books;
    private Login login;

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

        books = TestUtils.getBooks(author, Set.of(genre), Set.of(tag));
    }

    @Test
    void listBooks_noFilters_returnsBookListView() throws Exception {
        when(bookService.getPaginatedBooks(0, null, null, null, null, null, null))
                .thenReturn(new PageImpl<>(books));

        mockMvc.perform(get("/book/list"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("bookPage", "genres", "tags"))
                .andExpect(view().name("app/book/bookList"));

        verify(bookService).getPaginatedBooks(0, null, null, null, null, null, null);
        verify(genreRepository).findAll();
        verify(tagRepository).findAll();

        assertThat(applicationEvents.stream(ActivityEvent.class)).hasSize(1);
    }

    @Test
    void listBooks_ajaxRequest_returnsBookListFragment() throws Exception {
        when(bookService.getPaginatedBooks(0, null, null, null, null, null, null))
                .thenReturn(new PageImpl<>(books));

        mockMvc.perform(get("/book/list")
                        .header("X-Requested-With", "XMLHttpRequest"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("bookPage"))
                .andExpect(view().name("app/book/bookGridFragment :: bookGrid"));

        verify(bookService).getPaginatedBooks(0, null, null, null, null, null, null);
        verify(genreRepository, never()).findAll();
        verify(tagRepository, never()).findAll();

        assertThat(applicationEvents.stream(ActivityEvent.class)).hasSize(1);
    }

    @Test
    void listBooks_withFilters_passesFiltersToService() throws Exception {
        when(bookService.getPaginatedBooks(eq(0), isNull(),
                anyString(), anyString(), isNull(), isNull(), isNull()))
                .thenReturn(new PageImpl<>(books));

        mockMvc.perform(get("/book/list")
                        .param("query", "Book")
                        .param("sort", "title,asc"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("bookPage", "genres", "tags"))
                .andExpect(view().name("app/book/bookList"));

        verify(bookService).getPaginatedBooks(eq(0), isNull(), eq("Book"),
                eq("title,asc"), isNull(), isNull(), isNull());

        verify(genreRepository).findAll();
        verify(tagRepository).findAll();

        assertThat(applicationEvents.stream(ActivityEvent.class)).hasSize(1);
    }

    @Test
    void searchBooks_query_returnsJsonBookSuggestions() throws Exception {
        when(bookService.getPaginatedBooks(eq(0), eq(5),
                anyString(), isNull(), isNull(), isNull(), isNull()))
                .thenReturn(new PageImpl<>(books));

        Book book = books.getFirst();

        mockMvc.perform(get("/book/search")
                        .param("query", "Book"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(books.size()))
                .andExpect(jsonPath("$.[0].id").value(book.getId()))
                .andExpect(jsonPath("$.[0].title").value(book.getTitle()));
    }

    @Test
    void showBookDetail_existingBook_returnsBookDetailView() throws Exception {
        Book book = books.getFirst();
        stubBookReferenceData(book, true, true);

        mockMvc.perform(get("/book/" + book.getId()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("book", "readingProgresses", "tmpShelf",
                        "tmpProgress", "reviewDto", "relatedBooks"))
                .andExpect(model().attribute("book", book))
                .andExpect(view().name("app/book/book"));

        verify(bookHelper).setupReferenceData(any(LoginDetails.class), eq(book.getId()),
                any(ModelMap.class), eq(true), eq(true));

        assertThat(applicationEvents.stream(ActivityEvent.class)).hasSize(1);
    }

    @Test
    void showBookDetail_inactiveBook_throwsException() {
        Book book = books.getFirst();

        doThrow(AssertionError.class)
                .when(bookHelper).setupReferenceData(any(LoginDetails.class), eq(book.getId()),
                        any(ModelMap.class), eq(true), eq(true));

        assertThrows(ServletException.class,
                () -> mockMvc.perform(get("/book/" + book.getId())));
    }

    @Test
    void showReviews_existingBook_returnsReviewsFragment() throws Exception {
        Book book = books.getFirst();
        when(bookService.getBook(book.getId())).thenReturn(book);
        when(bookService.getReviewsByBook(book, 0)).thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/book/reviews/" + book.getId()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("reviews"))
                .andExpect(view().name("app/book/reviewList :: reviewList"));

        verify(bookService).getBook(book.getId());
        verify(bookService).getReviewsByBook(book, 0);
    }

    @Test
    void addReview_validDto_savesReviewAndReturnsFragment() throws Exception {
        Book book = books.getFirst();
        Review review = TestUtils.getReview(book, login, 5);

        when(bookService.saveReview(any(BookReviewDto.class),
                any(LoginDetails.class))).thenReturn(review);

        mockMvc.perform(post("/book/addReview")
                        .param("bookId", "1")
                        .param("rating", "5")
                        .param("content", "Review Content"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/book/" + book.getId()))
                .andExpect(flash().attribute("publishEvent", false));

        verify(bookService).saveReview(any(BookReviewDto.class), any(LoginDetails.class));
    }

    @Test
    void addReview_bindingErrors_returnsReviewFormFragment() throws Exception {
        Book book = books.getFirst();
        stubBookReferenceData(book, false, true);

        mockMvc.perform(post("/book/addReview")
                        .param("bookId", "1")
                        .param("content", "Review Content"))
                .andExpect(status().isOk())
                .andExpect(view().name("app/book/book"))
                .andExpect(model().attributeHasErrors("reviewDto"));

        verify(bookService, never()).saveReview(any(BookReviewDto.class), any(LoginDetails.class));
    }

    @Test
    void addToShelf_validRequest_redirects() throws Exception {
        mockMvc.perform(post("/book/addShelf")
                        .param("bookId", "1")
                        .param("shelfId", "1"))
                .andExpect(status().isOk());

        verify(bookService).addToShelf(any(LoginDetails.class), eq(1L), eq(1L));
    }

    @Test
    void removeFromShelf_validRequest_redirects() throws Exception {
        mockMvc.perform(post("/book/removeShelf")
                        .param("bookId", "1")
                        .param("shelfId", "1"))
                .andExpect(status().isOk());

        verify(bookService).removeFromShelf(any(LoginDetails.class), eq(1L), eq(1L));
    }

    @Test
    void updateProgress_validReadingProgress_redirects() throws Exception {
        Book book = books.getFirst();

        when(bookService.saveReadingProgress(any(ReadingProgress.class), any(LoginDetails.class)))
                .thenReturn(new ReadingProgress(1L, login, book, 0L, LocalDate.now(),
                        null, false, LocalDateTime.now()));

        mockMvc.perform(post("/book/updateProgress")
                        .param("book.id", "1")
                        .param("pagesRead", "1")
                        .param("startDate", "2026-01-01"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("publishEvent", false))
                .andExpect(redirectedUrl("/book/" + book.getId()));

        verify(bookService).saveReadingProgress(any(ReadingProgress.class), any(LoginDetails.class));
    }

    @Test
    void updateProgress_bindingErrors_returnView() throws Exception {
        Book book = books.getFirst();
        stubBookReferenceData(book, true, false);

        mockMvc.perform(post("/book/updateProgress")
                        .param("book.id", "1")
                        .param("pagesRead", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("app/book/book"))
                .andExpect(model().attributeHasErrors("tmpProgress"));

        verify(bookService, never()).saveReadingProgress(any(ReadingProgress.class), any(LoginDetails.class));
    }

    @Test
    void likeReview_existingReview_returnsLikeResponseDto() throws Exception {
        when(bookService.updateReviewLikes(eq(1L), any(LoginDetails.class)))
                .thenReturn(new ReviewLikeResponseDto(1L, true, 20));

        mockMvc.perform(post("/book/like")
                        .param("reviewId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isNotEmpty())
                .andExpect(jsonPath("$.reviewId").value(1))
                .andExpect(jsonPath("$.liked").value(true))
                .andExpect(jsonPath("$.likeCount").value(20));

        verify(bookService).updateReviewLikes(eq(1L), any(LoginDetails.class));
    }

    private void stubBookReferenceData(Book book, boolean addNewReview, boolean addNewProgress) {
        doAnswer(invocation -> {
            ModelMap model = invocation.getArgument(2);
            model.put("book", book);
            model.put("readingProgresses", List.of());
            model.put("tmpShelf", new Shelf());

            if (addNewProgress) {
                model.put("tmpProgress", new ReadingProgress());
            }

            if (addNewReview) {
                model.put("reviewDto", new BookReviewDto());
            }

            model.put("reviews", new PageImpl<>(List.of()));
            model.put("relatedBooks", List.of());

            model.put("defaultShelves", List.of());
            model.put("defaultShelf", TestUtils.getShelf(login, "Read", true));
            model.put("allShelves", List.of());
            model.put("shelvesTruncated", false);

            return null;
        }).when(bookHelper).setupReferenceData(any(LoginDetails.class),
                eq(book.getId()), any(ModelMap.class),
                eq(addNewReview), eq(addNewProgress));
    }
}
