package com.zedapps.bookshare.service.book;

import com.zedapps.bookshare.dto.book.BookReviewDto;
import com.zedapps.bookshare.dto.book.ReviewLikeResponseDto;
import com.zedapps.bookshare.dto.login.LoginDetails;
import com.zedapps.bookshare.entity.book.Book;
import com.zedapps.bookshare.entity.login.*;
import com.zedapps.bookshare.enums.ActivityType;
import com.zedapps.bookshare.enums.Status;
import com.zedapps.bookshare.repository.book.BookListRepository;
import com.zedapps.bookshare.repository.book.BookRepository;
import com.zedapps.bookshare.repository.book.ReviewRepository;
import com.zedapps.bookshare.repository.login.ReadingProgressRepository;
import com.zedapps.bookshare.repository.login.ShelvedBookRepository;
import com.zedapps.bookshare.service.activity.ActivityService;
import com.zedapps.bookshare.service.login.LoginService;
import jakarta.persistence.NoResultException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;

import java.time.LocalDate;
import java.util.*;

import static com.zedapps.bookshare.entity.login.Shelf.*;

/**
 * @author smzoha
 * @since 15/9/25
 **/
@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final BookListRepository bookListRepository;
    private final ReviewRepository reviewRepository;
    private final ShelvedBookRepository shelvedBookRepository;
    private final ReadingProgressRepository readingProgressRepository;

    private final LoginService loginService;
    private final ActivityService activityService;
    private final BookAdminService bookAdminService;

    public Book getBook(Long bookId) {
        return bookAdminService.getBook(bookId);
    }

    @Cacheable(cacheNames = "book-lists", key = "'books-' + #page + '-' + #pageSize",
            condition = "#query != null && #sort != null && #rating != null && #genre != null && #tag != null")
    public Page<Book> getPaginatedBooks(int page, Integer pageSize, String query, String sort, String rating, String genre, String tag) {
        Pageable pageable = PageRequest.of(page, Optional.ofNullable(pageSize).orElse(18));

        String[] sortComponents = StringUtils.isNotEmpty(sort) ? sort.split(",") : new String[2];

        if (StringUtils.isNotBlank(query)) {
            query = "%" + query.toLowerCase(LocaleContextHolder.getLocale()).trim() + "%";
        }

        return bookListRepository.getPaginatedBooks(pageable, query, rating, genre, tag, sortComponents[0], sortComponents[1]);
    }

    public List<Book> getRelatedBooks(Book book) {
        List<Book> relatedBooks = bookRepository.getRelatedBooks(book.getGenres(), book.getTags());
        relatedBooks.remove(book);

        return relatedBooks;
    }

    public Page<Review> getReviewsByBook(Book book, int pageNumber) {
        return reviewRepository.findReviewsByBookOrderByReviewDateDesc(book, PageRequest.of(pageNumber, 5));
    }

    public void setupReferenceData(LoginDetails loginDetails, Long bookId, ModelMap model,
                                   boolean addNewReview, boolean addNewProgress) {

        Book book = getBook(bookId);
        assert book.getStatus() == Status.ACTIVE : "Book is not in active status";

        model.put("book", book);

        if (Objects.nonNull(loginDetails)) {
            Login login = loginService.getLogin(loginDetails.getEmail());

            setupShelfReferenceData(login, model, book);
            model.put("readingProgresses", login.getReadingProgresses(book.getId()));
        }

        model.put("tmpShelf", new Shelf());
        if (addNewProgress) model.put("tmpProgress", new ReadingProgress());
        if (addNewReview) model.put("reviewDto", new BookReviewDto());

        model.put("reviews", getReviewsByBook(book, 0));
        model.put("relatedBooks", getRelatedBooks(book));
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

        return new ReviewLikeResponseDto(reviewId, false, review.getUserLikes().size());
    }

    @Transactional
    public void addToShelf(LoginDetails loginDetails, Long bookId, Long shelfId) {
        Login login = loginService.getLogin(loginDetails.getUsername());
        Book book = getBook(bookId);
        Shelf shelf = login.getShelf(shelfId);

        if (shelf.isDefaultShelf()) {
            for (Shelf s : login.getShelves()) {
                if (s.isDefaultShelf() && s.containsBook(book)) {
                    removeFromShelf(loginDetails, bookId, s.getId());
                }
            }
        }

        ShelvedBook shelvedBook = new ShelvedBook();
        shelvedBook.setLogin(login);
        shelvedBook.setShelf(shelf);
        shelvedBook.setBook(book);

        shelvedBookRepository.save(shelvedBook);

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

    private void setupShelfReferenceData(Login login, ModelMap model, Book book) {
        Map<String, Shelf> defaultShelves = new HashMap<>();
        List<Shelf> otherShelves = new ArrayList<>();

        for (Shelf shelf : login.getShelves()) {
            if (shelf.isDefaultShelf()) defaultShelves.put(shelf.getName(), shelf);
            else otherShelves.add(shelf);
        }

        Shelf defaultShelf = getDefaultShelf(book, defaultShelves);

        model.put("defaultShelves", defaultShelves.values());
        model.put("defaultShelf", defaultShelf);

        model.put("allShelves", otherShelves);
        model.put("shelvesTruncated", otherShelves.size() > 5);

        model.put("shelves", otherShelves.stream()
                .sorted(Comparator.comparing((Shelf s) -> s.containsBook(book)).reversed())
                .limit(5)
                .toList());
    }

    private Shelf getDefaultShelf(Book book, Map<String, Shelf> defaultShelves) {
        if (defaultShelves.containsKey(SHELF_READ) && defaultShelves.get(SHELF_READ).containsBook(book)) {
            return defaultShelves.get(SHELF_READ);

        } else if (defaultShelves.containsKey(SHELF_CURRENTLY_READING)
                && defaultShelves.get(SHELF_CURRENTLY_READING).containsBook(book)) {

            return defaultShelves.get(SHELF_CURRENTLY_READING);

        } else {
            return defaultShelves.get(SHELF_WANT_TO_READ);
        }
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
