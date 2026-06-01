package com.zedapps.bookshare.controller.api.book;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zedapps.bookshare.controller.AbstractWebMvcTest;
import com.zedapps.bookshare.dto.api.book.*;
import com.zedapps.bookshare.dto.api.shelf.ShelfDto;
import com.zedapps.bookshare.dto.book.ReviewLikeResponseDto;
import com.zedapps.bookshare.security.WithMockLoginDetails;
import com.zedapps.bookshare.service.auth.LoginDetails;
import com.zedapps.bookshare.service.book.BookApiService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author smzoha
 * @since 26/5/26
 **/
@WebMvcTest(BookApiController.class)
@WithMockLoginDetails
public class BookApiControllerTest extends AbstractWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookApiService bookApiService;

    @Autowired
    private ObjectMapper objectMapper;

    private BookDto bookDto;

    @BeforeEach
    void setUp() {
        bookDto = new BookDto("Test", "Test ISBN", null, null, 100L,
                LocalDate.now(), 5.0d,
                List.of(new AuthorDto("Test", "Author", null)),
                List.of("Action"), List.of("Fiction"),
                List.of());
    }

    @Test
    void getBook_existingId_returns200WithBookDto() throws Exception {
        when(bookApiService.getBookDto(anyLong(), eq(false))).thenReturn(bookDto);

        mockMvc.perform(get("/api/v1/book/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test"));
    }

    @Test
    void getBook_missingId_returns404() throws Exception {
        when(bookApiService.getBookDto(anyLong(), eq(false))).thenReturn(null);

        mockMvc.perform(get("/api/v1/book/2"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getBook_showReviewsTrue_passesShowReviewsFlagToService() throws Exception {
        when(bookApiService.getBookDto(anyLong(), eq(true))).thenReturn(bookDto);

        mockMvc.perform(get("/api/v1/book/1")
                        .param("showReviews", "true"))
                .andExpect(status().isOk());

        verify(bookApiService).getBookDto(anyLong(), eq(true));
    }

    @Test
    void getBookList_noFilters_returns200WithList() throws Exception {
        when(bookApiService.getBookDtoList(0, null, null, null, null, null))
                .thenReturn(List.of(bookDto));

        mockMvc.perform(get("/api/v1/book/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getBookList_withQueryAndSort_passesFiltersToService() throws Exception {
        when(bookApiService.getBookDtoList(0, null, "ASC", null, null, "Fiction"))
                .thenReturn(List.of(bookDto));

        mockMvc.perform(get("/api/v1/book/list")
                        .param("sort", "ASC")
                        .param("tag", "Fiction"))
                .andExpect(status().isOk());

        verify(bookApiService).getBookDtoList(0, null, "ASC", null, null, "Fiction");
    }

    @Test
    void getBookList_noResults_returns200WithEmptyList() throws Exception {
        when(bookApiService.getBookDtoList(0, null, null, null, null, null))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/v1/book/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void searchBooks_query_returns200WithList() throws Exception {
        when(bookApiService.searchBookList(anyString())).thenReturn(List.of(bookDto));

        mockMvc.perform(get("/api/v1/book/search")
                        .param("query", "Test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(bookApiService).searchBookList("Test");
    }

    @Test
    void searchBooks_noResults_returns200WithEmptyList() throws Exception {
        when(bookApiService.searchBookList(anyString())).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/book/search")
                        .param("query", "nonexistent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void addReview_validRequest_returns200WithReviewDto() throws Exception {
        ReviewDto reviewDto = new ReviewDto("test@test.com", "Test Comment",
                LocalDateTime.now(), 5);

        ReviewRequest request = new ReviewRequest(5, "Test Comment");

        when(bookApiService.saveReview(eq(1L), any(ReviewRequest.class), any(LoginDetails.class)))
                .thenReturn(reviewDto);

        mockMvc.perform(post("/api/v1/book/1/review")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reviewedBy").value(reviewDto.reviewedBy()))
                .andExpect(jsonPath("$.comment").value(reviewDto.comment()))
                .andExpect(jsonPath("$.rating").value(reviewDto.rating()));
    }

    @Test
    void addReview_bindingErrors_returns400WithErrorResponseDto() throws Exception {
        ReviewRequest request = new ReviewRequest(null, "Test Comment");

        mockMvc.perform(post("/api/v1/book/1/review")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.length()").value(1));
    }

    @Test
    void saveReadingProgress_validRequest_returns200WithProgressDto() throws Exception {
        ReadingProgressRequest request = new ReadingProgressRequest(1L, 10L,
                LocalDate.now().minusDays(10), LocalDate.now(), false);

        when(bookApiService.saveReadingProgress(eq(1L),
                any(ReadingProgressRequest.class), any(LoginDetails.class)))
                .thenReturn(new ReadingProgressDto(bookDto.title(), bookDto.isbn(), "test@test.com",
                        request.pagesRead(), request.startDate(), request.endDate(), request.completed()));

        mockMvc.perform(post("/api/v1/book/1/progress")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookTitle").value(bookDto.title()))
                .andExpect(jsonPath("$.isbn").value(bookDto.isbn()))
                .andExpect(jsonPath("$.login").value("test@test.com"))
                .andExpect(jsonPath("$.pagesRead").value(request.pagesRead()))
                .andExpect(jsonPath("$.completed").value(request.completed()));
    }

    @Test
    void saveReadingProgress_bindingErrors_returns400WithErrorResponseDto() throws Exception {
        ReadingProgressRequest request = new ReadingProgressRequest(1L, null,
                null, LocalDate.now(), false);

        mockMvc.perform(post("/api/v1/book/1/progress")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.length()").value(2));
    }

    @Test
    void addToShelf_validRequest_returns200WithShelfDto() throws Exception {
        ShelfDto shelfDto = new ShelfDto("Shelf 1", "test@test.com", 1, false);

        when(bookApiService.addToShelf(eq(1L), eq(1L), any(LoginDetails.class)))
                .thenReturn(shelfDto);

        mockMvc.perform(post("/api/v1/book/1/shelf")
                        .param("shelfId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(shelfDto.name()))
                .andExpect(jsonPath("$.login").value(shelfDto.login()))
                .andExpect(jsonPath("$.bookCount").value(shelfDto.bookCount()))
                .andExpect(jsonPath("$.defaultShelf").value(shelfDto.defaultShelf()));

        verify(bookApiService).addToShelf(eq(1L), eq(1L), any(LoginDetails.class));
    }

    @Test
    void addToShelf_invalidShelfRequest_returns400() throws Exception {
        when(bookApiService.isInvalidShelfRequest(isNull(), eq(1L), any(LoginDetails.class)))
                .thenReturn(true);

        mockMvc.perform(post("/api/v1/book/1/shelf")
                        .param("shelfId", "1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.globalErrors.length()").value(1));
    }

    @Test
    void removeFromShelf_validRequest_returns200WithShelfDto() throws Exception {
        ShelfDto shelfDto = new ShelfDto("Shelf 1", "test@test.com", 1, false);

        when(bookApiService.removeFromShelf(eq(1L), eq(1L), any(LoginDetails.class)))
                .thenReturn(shelfDto);

        mockMvc.perform(delete("/api/v1/book/1/shelf")
                        .param("shelfId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(shelfDto.name()))
                .andExpect(jsonPath("$.login").value(shelfDto.login()))
                .andExpect(jsonPath("$.bookCount").value(shelfDto.bookCount()))
                .andExpect(jsonPath("$.defaultShelf").value(shelfDto.defaultShelf()));

        verify(bookApiService).removeFromShelf(eq(1L), eq(1L), any(LoginDetails.class));
    }

    @Test
    void removeFromShelf_invalidShelfRequest_returns400() throws Exception {
        when(bookApiService.isInvalidShelfRequest(eq(1L), eq(1L), any(LoginDetails.class)))
                .thenReturn(true);

        mockMvc.perform(delete("/api/v1/book/1/shelf")
                        .param("shelfId", "1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.globalErrors.length()").value(1));
    }

    @Test
    void likeReview_existingReview_returns200WithLikeResponseDto() throws Exception {
        ReviewLikeResponseDto response = new ReviewLikeResponseDto(1L, true, 20);

        when(bookApiService.isValidReviewRequest(eq(1L))).thenReturn(true);
        when(bookApiService.likeReview(eq(1L), any(LoginDetails.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/book/review/1/like"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reviewId").value(response.getReviewId()))
                .andExpect(jsonPath("$.liked").value(response.getLiked()))
                .andExpect(jsonPath("$.likeCount").value(response.getLikeCount()));
    }

    @Test
    void likeReview_nonExistingReview_returns400WithErrorResponseDto() throws Exception {
        when(bookApiService.isValidReviewRequest(anyLong())).thenReturn(false);

        mockMvc.perform(post("/api/v1/book/review/1/like"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.globalErrors.length()").value(1));
    }
}
