package com.zedapps.bookshare.controller.api.book;

import com.zedapps.bookshare.dto.api.book.AuthorDto;
import com.zedapps.bookshare.dto.api.book.BookDto;
import com.zedapps.bookshare.dto.api.book.ReviewDto;
import com.zedapps.bookshare.entity.book.Book;
import com.zedapps.bookshare.entity.book.Genre;
import com.zedapps.bookshare.entity.book.Tag;
import com.zedapps.bookshare.service.book.BookService;
import com.zedapps.bookshare.util.Utils;
import jakarta.persistence.NoResultException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.time.format.DateTimeFormatter;
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

    private final BookService bookService;

    @GetMapping("/{id}")
    public ResponseEntity<BookDto> getBook(@PathVariable Long id) {
        Book book;

        try {
            book = bookService.getBook(id);
        } catch (NoResultException e) {
            return ResponseEntity.notFound().build();
        }

        BookDto dto = createDto(book);

        return ResponseEntity.ok(dto);
    }

    @GetMapping("/list")
    public ResponseEntity<List<BookDto>> getBookList(@RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(required = false) String query,
                                                     @RequestParam(required = false) String sort,
                                                     @RequestParam(required = false) String rating,
                                                     @RequestParam(required = false) String genre,
                                                     @RequestParam(required = false) String tag) {

        List<BookDto> bookDtoList = bookService.getPaginatedBooks(page, null, query, sort, rating, genre, tag)
                .stream()
                .map(this::createDto)
                .toList();

        return ResponseEntity.ok(bookDtoList);
    }

    private BookDto createDto(Book book) {
        List<AuthorDto> authorDtoList = book.getAuthors().stream()
                .map(author -> new AuthorDto(author.getFirstName(), author.getLastName(),
                        Objects.nonNull(author.getLogin()) ? author.getLogin().getEmail() : null))
                .toList();

        List<String> genres = book.getGenres().stream().map(Genre::getName).toList();
        List<String> tags = book.getTags().stream().map(Tag::getName).toList();

        List<ReviewDto> reviewDtoList = book.getReviews().stream()
                .map(review -> new ReviewDto(review.getUser().getName(), review.getContent(),
                        DateTimeFormatter.ofPattern("MM/dd/yyyy").format(review.getReviewDate()),
                        review.getRating()))
                .toList();

        String imageUrl = Objects.nonNull(book.getImage())
                ? ServletUriComponentsBuilder.fromCurrentContextPath()
                  .path("/image/{id}")
                  .buildAndExpand(book.getImage().getId())
                  .toUriString()
                : "";

        return new BookDto(book.getTitle(), book.getIsbn(), Utils.cleanHtml(book.getDescription()),
                imageUrl, book.getPages(), book.getPublicationDate(), authorDtoList, genres, tags, reviewDtoList);
    }
}

