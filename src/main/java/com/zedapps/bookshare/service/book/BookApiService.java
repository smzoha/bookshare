package com.zedapps.bookshare.service.book;

import com.zedapps.bookshare.dto.api.book.AuthorDto;
import com.zedapps.bookshare.dto.api.book.BookDto;
import com.zedapps.bookshare.dto.api.book.ReviewDto;
import com.zedapps.bookshare.entity.book.Book;
import com.zedapps.bookshare.entity.book.Genre;
import com.zedapps.bookshare.entity.book.Tag;
import com.zedapps.bookshare.util.Utils;
import jakarta.persistence.NoResultException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author smzoha
 * @since 18/4/26
 **/
@Service
@RequiredArgsConstructor
public class BookApiService {

    private final BookService bookService;

    public BookDto getBookDto(Long id) {
        Book book;

        try {
            book = bookService.getBook(id);

        } catch (NoResultException e) {
            return null;
        }

        return createDto(book, true);
    }

    public List<BookDto> getBookDtoList(int page, String query, String sort,
                                        String rating, String genre, String tag) {

        return bookService.getPaginatedBooks(page, null, query, sort, rating, genre, tag)
                .stream()
                .map(book -> createDto(book, false))
                .toList();
    }

    private BookDto createDto(Book book, boolean includeReview) {
        List<AuthorDto> authorDtoList = getAuthorDtoList(book);
        List<ReviewDto> reviewDtoList = includeReview ? getReviewDtoList(book) : Collections.emptyList();

        List<String> genres = book.getGenres().stream().map(Genre::getName).toList();
        List<String> tags = book.getTags().stream().map(Tag::getName).toList();

        String imageUrl = getImageUrl(book);

        return new BookDto(book.getTitle(), book.getIsbn(), Utils.cleanHtml(book.getDescription()),
                imageUrl, book.getPages(), book.getPublicationDate(), book.getAverageRating(),
                authorDtoList, genres, tags, reviewDtoList);
    }

    private List<AuthorDto> getAuthorDtoList(Book book) {
        return book.getAuthors().stream()
                .map(author -> new AuthorDto(author.getFirstName(), author.getLastName(),
                        Objects.nonNull(author.getLogin()) ? author.getLogin().getEmail() : null))
                .toList();
    }

    private List<ReviewDto> getReviewDtoList(Book book) {
        return book.getReviews().stream()
                .map(review -> new ReviewDto(review.getUser().getName(), review.getContent(),
                        DateTimeFormatter.ofPattern("MM/dd/yyyy").format(review.getReviewDate()),
                        review.getRating()))
                .toList();
    }

    private static String getImageUrl(Book book) {
        return Objects.nonNull(book.getImage())
                ? ServletUriComponentsBuilder.fromCurrentContextPath()
                  .path("/image/{id}")
                  .buildAndExpand(book.getImage().getId())
                  .toUriString()
                : "";
    }
}
