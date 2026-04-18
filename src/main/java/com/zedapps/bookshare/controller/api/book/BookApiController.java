package com.zedapps.bookshare.controller.api.book;

import com.zedapps.bookshare.dto.api.book.AuthorDto;
import com.zedapps.bookshare.dto.api.book.BookDto;
import com.zedapps.bookshare.dto.api.book.ReviewDto;
import com.zedapps.bookshare.entity.book.Book;
import com.zedapps.bookshare.entity.book.Genre;
import com.zedapps.bookshare.entity.book.Tag;
import com.zedapps.bookshare.enums.Status;
import com.zedapps.bookshare.repository.book.BookRepository;
import com.zedapps.bookshare.util.Utils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @author smzoha
 * @since 18/4/26
 **/
@RestController
@RequestMapping("/api/v1/book")
@RequiredArgsConstructor
public class BookApiController {

    private final BookRepository bookRepository;

    @GetMapping("/{id}")
    public ResponseEntity<BookDto> getBook(@PathVariable Long id) {
        Optional<Book> bookOptional = bookRepository.findBookById(id);

        if (bookOptional.isEmpty() || bookOptional.get().getStatus() != Status.ACTIVE) {
            return ResponseEntity.notFound().build();
        }

        BookDto dto = createDto(bookOptional.get());

        return ResponseEntity.ok(dto);
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

