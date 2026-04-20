package com.zedapps.bookshare.service.book;

import com.zedapps.bookshare.dto.api.book.*;
import com.zedapps.bookshare.dto.api.shelf.ShelfDto;
import com.zedapps.bookshare.dto.book.BookReviewDto;
import com.zedapps.bookshare.dto.book.ReviewLikeResponseDto;
import com.zedapps.bookshare.dto.login.LoginDetails;
import com.zedapps.bookshare.entity.book.Book;
import com.zedapps.bookshare.entity.book.Genre;
import com.zedapps.bookshare.entity.book.Tag;
import com.zedapps.bookshare.entity.login.ReadingProgress;
import com.zedapps.bookshare.entity.login.Review;
import com.zedapps.bookshare.entity.login.Shelf;
import com.zedapps.bookshare.repository.book.ReviewRepository;
import com.zedapps.bookshare.repository.login.ReadingProgressRepository;
import com.zedapps.bookshare.repository.login.ShelfRepository;
import com.zedapps.bookshare.util.Utils;
import jakarta.persistence.NoResultException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @author smzoha
 * @since 18/4/26
 **/
@Service
@RequiredArgsConstructor
public class BookApiService {

    private final BookService bookService;
    private final ShelfRepository shelfRepository;
    private final ReviewRepository reviewRepository;
    private final ReadingProgressRepository readingProgressRepository;

    public BookDto getBookDto(Long id, boolean showReviews) {
        Book book;

        try {
            book = bookService.getBook(id);

        } catch (NoResultException e) {
            return null;
        }

        return createDto(book, showReviews);
    }

    public List<BookDto> getBookDtoList(int page, String query, String sort,
                                        String rating, String genre, String tag) {

        return bookService.getPaginatedBooks(page, null, query, sort, rating, genre, tag)
                .stream()
                .map(book -> createDto(book, false))
                .toList();
    }

    public List<BookDto> searchBookList(String query) {
        return bookService.getPaginatedBooks(0, 5, query, null, null, null, null)
                .stream()
                .map(book -> createDto(book, false))
                .toList();
    }

    @Transactional
    public ReviewDto saveReview(Long bookId, ReviewRequest request, LoginDetails loginDetails) {
        BookReviewDto bookReviewDto = new BookReviewDto(bookId, request.rating(), request.content());
        Review review = bookService.saveReview(bookReviewDto, loginDetails);

        return new ReviewDto(review.getUser().getName(), review.getContent(), review.getReviewDate(), review.getRating());
    }

    @Transactional
    public ReadingProgressDto saveReadingProgress(Long bookId, ReadingProgressRequest progressRequest, LoginDetails loginDetails) {
        Optional<ReadingProgress> readingProgressOptional = Objects.nonNull(progressRequest.progressId())
                ? readingProgressRepository.findById(progressRequest.progressId())
                : Optional.empty();

        ReadingProgress readingProgress = getReadingProgress(bookId, progressRequest, readingProgressOptional);
        readingProgress = bookService.saveReadingProgress(readingProgress, loginDetails);

        return new ReadingProgressDto(readingProgress.getBook().getTitle(),
                readingProgress.getBook().getIsbn(),
                readingProgress.getUser().getName(),
                readingProgress.getPagesRead(),
                readingProgress.getStartDate(),
                readingProgress.getEndDate(),
                readingProgress.isCompleted());
    }

    @Transactional
    public ShelfDto addToShelf(Long bookId, Long shelfId, LoginDetails loginDetails) {
        bookService.addToShelf(loginDetails, bookId, shelfId);

        return getShelfResponseDto(shelfId, loginDetails);
    }

    @Transactional
    public ShelfDto removeFromShelf(Long bookId, Long shelfId, LoginDetails loginDetails) {
        bookService.removeFromShelf(loginDetails, bookId, shelfId);

        return getShelfResponseDto(shelfId, loginDetails);
    }

    @Transactional
    public ReviewLikeResponseDto likeReview(Long reviewId, LoginDetails loginDetails) {
        return bookService.updateReviewLikes(reviewId, loginDetails);
    }

    public boolean isInvalidShelfRequest(Long bookId, Long shelfId, LoginDetails loginDetails) {
        Optional<Shelf> shelf = shelfRepository.findById(shelfId);

        boolean isValid = shelf.isPresent() && Objects.equals(shelf.get().getUser().getEmail(), loginDetails.getEmail());

        if (isValid && Objects.nonNull(bookId)) {
            isValid = shelf.get().getBooks().stream().anyMatch(sb -> Objects.equals(sb.getBook().getId(), bookId));
        }

        return !isValid;
    }

    public boolean isValidReviewRequest(Long reviewId) {
        return reviewRepository.findById(reviewId).isPresent();
    }

    public BookDto createDto(Book book, boolean includeReview) {
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
                .map(review -> new ReviewDto(review.getUser().getName(), review.getContent(), review.getReviewDate(),
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

    private ReadingProgress getReadingProgress(Long bookId, ReadingProgressRequest progressRequest,
                                               Optional<ReadingProgress> readingProgressOptional) {

        ReadingProgress readingProgress = readingProgressOptional.orElse(new ReadingProgress());

        readingProgress.setBook(bookService.getBook(bookId));
        readingProgress.setPagesRead(progressRequest.pagesRead());
        readingProgress.setStartDate(progressRequest.startDate());
        readingProgress.setEndDate(progressRequest.endDate());
        readingProgress.setCompleted(progressRequest.completed());

        return readingProgress;
    }

    private ShelfDto getShelfResponseDto(Long shelfId, LoginDetails loginDetails) {
        Shelf shelf = shelfRepository.findById(shelfId).orElseThrow(NoResultException::new);

        return new ShelfDto(shelf.getName(), loginDetails.getEmail(), shelf.getBooks().size(), shelf.isDefaultShelf());
    }
}
