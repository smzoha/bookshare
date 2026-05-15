package com.zedapps.bookshare.service.book;

import com.zedapps.bookshare.dto.book.BookReviewDto;
import com.zedapps.bookshare.dto.book.ReviewLikeResponseDto;
import com.zedapps.bookshare.entity.book.Book;
import com.zedapps.bookshare.entity.book.Genre;
import com.zedapps.bookshare.entity.book.Tag;
import com.zedapps.bookshare.entity.login.*;
import com.zedapps.bookshare.enums.ActivityType;
import com.zedapps.bookshare.repository.book.BookRepository;
import com.zedapps.bookshare.repository.login.ReadingProgressRepository;
import com.zedapps.bookshare.repository.login.ReviewRepository;
import com.zedapps.bookshare.repository.login.ShelvedBookRepository;
import com.zedapps.bookshare.service.activity.ActivityService;
import com.zedapps.bookshare.service.auth.LoginDetails;
import com.zedapps.bookshare.service.login.LoginService;
import com.zedapps.bookshare.service.shelf.ShelfService;
import jakarta.persistence.NoResultException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Hibernate;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

/**
 * @author smzoha
 * @since 15/9/25
 **/
@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final ReviewRepository reviewRepository;
    private final ShelvedBookRepository shelvedBookRepository;
    private final ReadingProgressRepository readingProgressRepository;

    private final LoginService loginService;
    private final ActivityService activityService;
    private final BookAdminService bookAdminService;
    private final ShelfService shelfService;

    @Transactional(readOnly = true)
    public Book getBook(Long bookId) {
        return bookAdminService.getBook(bookId);
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "book-lists", key = "'books-' + #page + '-' + #pageSize",
            condition = "#query != null && #sort != null && #rating != null && #genre != null && #tag != null")
    public Page<Book> getPaginatedBooks(int page, Integer pageSize, String query, String sort, String rating, String genre, String tag) {
        Pageable pageable = PageRequest.of(page, Optional.ofNullable(pageSize).orElse(18));

        String[] sortComponents = StringUtils.isNotEmpty(sort) ? sort.split(",") : new String[2];

        query = StringUtils.isNotBlank(query)
                ? "%" + query.toLowerCase(LocaleContextHolder.getLocale()).trim() + "%"
                : null;

        Page<Book> books = bookRepository.getPaginatedBooks(pageable, query, rating, genre, tag, sortComponents[0], sortComponents[1]);

        books.forEach(b -> {
            Hibernate.initialize(b.getAuthors());
            Hibernate.initialize(b.getGenres());
            Hibernate.initialize(b.getTags());
        });

        return books;
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "book-lists", key = "'related-' + #genres.hashCode() + '-' + #tags.hashCode()")
    public List<Book> getRelatedBooks(Book book, Set<Genre> genres, Set<Tag> tags) {
        List<Book> relatedBooks = bookRepository.getRelatedBooks(genres, tags);
        relatedBooks.remove(book);

        return relatedBooks;
    }

    @Transactional(readOnly = true)
    public Page<Review> getReviewsByBook(Book book, int pageNumber) {
        return reviewRepository.findReviewsByBookOrderByReviewDateDesc(book, PageRequest.of(pageNumber, 5));
    }

    @Transactional
    public Review saveReview(BookReviewDto reviewDto, LoginDetails loginDetails) {
        Login login = loginService.getLogin(loginDetails.getUsername());
        Book book = getBook(reviewDto.getBookId());

        Review review = createReviewFromDto(reviewDto, book, login);
        review = reviewRepository.save(review);

        activityService.saveActivityOutbox(ActivityType.BOOK_ADD_REVIEW,
                review.getId(),
                Map.of(
                        "actionBy", loginDetails.getEmail(),
                        "bookId", book.getId(),
                        "reviewId", review.getId()
                ));

        return review;
    }

    @Transactional
    public ReviewLikeResponseDto updateReviewLikes(Long reviewId, LoginDetails loginDetails) {
        Review review = reviewRepository.findById(reviewId).orElseThrow(NoResultException::new);
        Login login = loginService.getLogin(loginDetails.getUsername());

        boolean liked = !review.getUserLikes().contains(login);

        if (liked) {
            review.getUserLikes().add(login);
        } else {
            review.getUserLikes().remove(login);
        }

        review = reviewRepository.save(review);

        activityService.saveActivityOutbox(liked ? ActivityType.BOOK_LIKE_REVIEW : ActivityType.BOOK_REMOVE_LIKE_REVIEW,
                review.getId(),
                Map.of(
                        "actionBy", loginDetails.getEmail(),
                        "reviewedBy", review.getUser().getEmail(),
                        "bookId", review.getBook().getId(),
                        "reviewId", review.getId()
                ));

        return new ReviewLikeResponseDto(reviewId, liked, review.getUserLikes().size());
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = "shelf-lists", key = "#loginDetails.email"),
            @CacheEvict(cacheNames = "shelves", key = "#shelfId")
    })
    public void addToShelf(LoginDetails loginDetails, Long bookId, Long shelfId) {
        Login login = loginService.getLogin(loginDetails.getUsername());
        Book book = getBook(bookId);
        Shelf shelf = login.getShelf(shelfId);

        if (shelf.isDefaultShelf()) {
            for (Shelf s : shelfService.getShelvesForCollection(login.getEmail())) {
                if (s.isDefaultShelf() && s.containsBook(book)) {
                    removeFromShelf(loginDetails, bookId, s.getId());
                }
            }
        }

        ShelvedBook shelvedBook = new ShelvedBook();
        shelvedBook.setLogin(login);
        shelvedBook.setShelf(shelf);
        shelvedBook.setBook(book);

        shelvedBook = shelvedBookRepository.save(shelvedBook);

        activityService.saveActivityOutbox(ActivityType.BOOK_ADD_TO_SHELF,
                shelf.getId(),
                Map.of(
                        "actionBy", loginDetails.getEmail(),
                        "bookId", book.getId(),
                        "shelfId", shelf.getId(),
                        "shelvedBookId", shelvedBook.getId()
                ));
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = "shelf-lists", key = "#loginDetails.email"),
            @CacheEvict(cacheNames = "shelves", key = "#shelfId")
    })
    public void removeFromShelf(LoginDetails loginDetails, Long bookId, Long shelfId) {
        Login login = loginService.getLogin(loginDetails.getUsername());
        Book book = getBook(bookId);
        Shelf shelf = login.getShelf(shelfId);

        ShelvedBook shelvedBook = shelvedBookRepository.findShelvedBookByLoginAndShelfAndBook(login, shelf, book)
                .orElseThrow();

        shelvedBookRepository.delete(shelvedBook);

        activityService.saveActivityOutbox(ActivityType.BOOK_REMOVE_FROM_SHELF,
                shelf.getId(),
                Map.of(
                        "actionBy", loginDetails.getEmail(),
                        "bookId", book.getId(),
                        "shelfId", shelf.getId(),
                        "shelvedBookId", shelvedBook.getId()
                ));
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = "logins", key = "#loginDetails.email"),
            @CacheEvict(cacheNames = "logins", key = "#loginDetails.handle", condition = "#loginDetails.handle != null")
    })
    public ReadingProgress saveReadingProgress(ReadingProgress readingProgress, LoginDetails loginDetails) {
        if (readingProgress.getId() != null) {
            ReadingProgress persistedReadingProgress = readingProgressRepository.findById(readingProgress.getId())
                    .orElseThrow(NoResultException::new);

            assert Objects.equals(persistedReadingProgress.getUser().getEmail(), loginDetails.getEmail());
        }

        readingProgress.setUser(loginService.getLogin(loginDetails.getEmail()));

        if (readingProgress.isCompleted()) {
            Book book = getBook(readingProgress.getBook().getId());
            readingProgress.setPagesRead(book.getPages());

            if (Objects.isNull(readingProgress.getEndDate())) {
                readingProgress.setEndDate(LocalDate.now());
            }
        }

        readingProgress = readingProgressRepository.save(readingProgress);

        activityService.saveActivityOutbox(ActivityType.BOOK_UPDATE_READING_PROGRESS,
                readingProgress.getId(),
                Map.of(
                        "actionBy", loginDetails.getEmail(),
                        "bookId", readingProgress.getBook().getId(),
                        "progressId", readingProgress.getId(),
                        "pagesRead", Objects.toString(readingProgress.getPagesRead(), ""),
                        "totalPages", Objects.toString(readingProgress.getBook().getPages(), ""),
                        "completed", Objects.toString(readingProgress.isCompleted(), ""),
                        "startDate", Objects.toString(readingProgress.getStartDate(), ""),
                        "endDate", Objects.toString(readingProgress.getEndDate(), "")
                ));

        return readingProgress;
    }

    private Review createReviewFromDto(BookReviewDto reviewDto, Book book, Login login) {
        Review review = new Review();
        review.setRating(reviewDto.getRating());
        review.setContent(reviewDto.getContent());
        review.setBook(book);
        review.setUser(login);

        return review;
    }
}
