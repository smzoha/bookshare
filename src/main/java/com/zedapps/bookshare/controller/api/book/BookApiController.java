package com.zedapps.bookshare.controller.api.book;

import com.zedapps.bookshare.dto.api.book.BookDto;
import com.zedapps.bookshare.service.book.BookApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<BookDto> getBook(@PathVariable Long id) {
        BookDto bookDto = bookApiService.getBookDto(id);

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
}

