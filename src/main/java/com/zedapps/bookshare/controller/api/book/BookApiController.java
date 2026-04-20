package com.zedapps.bookshare.controller.api.book;

import com.zedapps.bookshare.dto.api.book.*;
import com.zedapps.bookshare.dto.login.LoginDetails;
import com.zedapps.bookshare.service.book.BookApiService;
import com.zedapps.bookshare.util.Utils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

/**
 * @author smzoha
 * @since 18/4/26
 **/
@RestController
@RequestMapping("/api/v1/book")
@RequiredArgsConstructor
public class BookApiController {

    private final BookApiService bookApiService;

    @GetMapping("/{id}")
    public ResponseEntity<BookDto> getBook(@PathVariable Long id,
                                           @RequestParam(required = false, defaultValue = "false") boolean showReviews) {

        BookDto bookDto = bookApiService.getBookDto(id, showReviews);

        return Objects.nonNull(bookDto)
                ? ResponseEntity.ok(bookDto)
                : ResponseEntity.notFound().build();
    }

    @GetMapping("/list")
    public ResponseEntity<List<BookDto>> getBookList(@RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(required = false) String query,
                                                     @RequestParam(required = false) String sort,
                                                     @RequestParam(required = false) String rating,
                                                     @RequestParam(required = false) String genre,
                                                     @RequestParam(required = false) String tag) {

        return ResponseEntity.ok(bookApiService.getBookDtoList(page, query, sort, rating, genre, tag));
    }

    @GetMapping("/search")
    public ResponseEntity<List<BookDto>> searchBooks(@RequestParam String query) {
        return ResponseEntity.ok(bookApiService.searchBookList(query));
    }

    @PostMapping("/{id}/review")
    public ResponseEntity<?> saveReview(@PathVariable Long id,
                                        @Valid @RequestBody ReviewRequest reviewRequest,
                                        Errors errors,
                                        @AuthenticationPrincipal LoginDetails loginDetails) {

        if (errors.hasErrors()) {
            return ResponseEntity.badRequest().body(Utils.getErrorResponseDto(errors));
        }

        ReviewDto review = bookApiService.saveReview(id, reviewRequest, loginDetails);

        return ResponseEntity.ok().body(review);
    }

    @PostMapping("/{id}/progress")
    public ResponseEntity<?> updateReadingProgress(@PathVariable Long id,
                                                   @Valid @RequestBody ReadingProgressRequest progressRequest,
                                                   Errors errors,
                                                   @AuthenticationPrincipal LoginDetails loginDetails) {

        if (errors.hasErrors()) {
            return ResponseEntity.badRequest().body(Utils.getErrorResponseDto(errors));
        }

        ReadingProgressDto progressDto = bookApiService.saveReadingProgress(id, progressRequest, loginDetails);

        return ResponseEntity.ok().body(progressDto);
    }
}

