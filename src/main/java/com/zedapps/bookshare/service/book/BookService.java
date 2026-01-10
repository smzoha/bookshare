package com.zedapps.bookshare.service.book;

import com.zedapps.bookshare.dto.book.BookReviewDto;
import com.zedapps.bookshare.dto.book.ReviewLikeResponseDto;
import com.zedapps.bookshare.dto.login.LoginDetails;
import com.zedapps.bookshare.entity.book.Book;
import com.zedapps.bookshare.entity.login.Login;
import com.zedapps.bookshare.entity.login.Review;
import com.zedapps.bookshare.entity.login.Shelf;
import com.zedapps.bookshare.entity.login.ShelvedBook;
import com.zedapps.bookshare.repository.book.BookListRepository;
import com.zedapps.bookshare.repository.book.BookRepository;
import com.zedapps.bookshare.repository.book.ReviewRepository;
import com.zedapps.bookshare.repository.login.ShelvedBookRepository;
import com.zedapps.bookshare.service.login.LoginService;
import jakarta.persistence.NoResultException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;

import java.util.*;

import static com.zedapps.bookshare.entity.login.Shelf.*;

/**
 * @author smzoha
 * @since 15/9/25
 **/
@Service
public class BookService {

    private final BookRepository bookRepository;
    private final BookListRepository bookListRepository;
    private final ReviewRepository reviewRepository;
    private final LoginService loginService;
    private final ShelvedBookRepository shelvedBookRepository;

    public BookService(BookRepository bookRepository,
                       BookListRepository bookListRepository,
                       ReviewRepository reviewRepository,
                       LoginService loginService,
                       ShelvedBookRepository shelvedBookRepository) {

        this.bookRepository = bookRepository;
        this.bookListRepository = bookListRepository;
        this.reviewRepository = reviewRepository;
        this.loginService = loginService;
        this.shelvedBookRepository = shelvedBookRepository;
    }

    public Book getBook(Long bookId) {
        return bookRepository.findBookById(bookId).orElseThrow(NoResultException::new);
    }

    public Page<Book> getPaginatedBooks(int page, String sort, String rating, String genre, String tag) {
        Pageable pageable = PageRequest.of(page, 18);

        String[] sortComponents = StringUtils.isNotEmpty(sort) ? sort.split(",") : new String[2];
        return bookListRepository.getPaginatedBooks(pageable, rating, genre, tag, sortComponents[0], sortComponents[1]);
    }

    public List<Book> getRelatedBooks(Book book) {
        List<Book> relatedBooks = bookRepository.getRelatedBooks(book.getGenres(), book.getTags());
        relatedBooks.remove(book);

        return relatedBooks;
    }

    public Page<Review> getReviewsByBook(Book book, int pageNumber) {
        return reviewRepository.findReviewsByBookOrderByReviewDateDesc(book, PageRequest.of(pageNumber, 5));
    }

    public void setupReferenceData(LoginDetails loginDetails, Long bookId, ModelMap model) {
        Book book = getBook(bookId);
        model.put("book", book);

        setupShelfReferenceData(loginDetails, model, book);

        model.put("tmpShelf", new Shelf());
        model.put("reviewDto", new BookReviewDto());

        model.put("reviews", getReviewsByBook(book, 0));
        model.put("relatedBooks", getRelatedBooks(book));
    }

    @Transactional
    public Review saveReview(BookReviewDto reviewDto, LoginDetails loginDetails) {
        Login login = loginService.getLogin(loginDetails.getUsername());
        Book book = getBook(reviewDto.getBookId());

        Review review = createReviewFromDto(reviewDto, book, login);
        review = reviewRepository.save(review);

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

        return new ReviewLikeResponseDto(reviewId, false, review.getUserLikes().size());
    }

    @Transactional
    public void addToShelf(LoginDetails loginDetails, Long bookId, Long shelfId) {
        Login login = loginService.getLogin(loginDetails.getUsername());
        Book book = getBook(bookId);
        Shelf shelf = login.getShelf(shelfId);

        ShelvedBook shelvedBook = new ShelvedBook();
        shelvedBook.setLogin(login);
        shelvedBook.setShelf(shelf);
        shelvedBook.setBook(book);

        shelvedBookRepository.save(shelvedBook);
    }

    @Transactional
    public void removeFromShelf(LoginDetails loginDetails, Long bookId, Long shelfId) {
        Login login = loginService.getLogin(loginDetails.getUsername());
        Book book = getBook(bookId);
        Shelf shelf = login.getShelf(shelfId);

        ShelvedBook shelvedBook = shelvedBookRepository.findShelvedBookByLoginAndShelfAndBook(login, shelf, book)
                .orElseThrow();

        shelvedBookRepository.delete(shelvedBook);
    }

    private void setupShelfReferenceData(LoginDetails loginDetails, ModelMap model, Book book) {
        if (Objects.nonNull(loginDetails)) {
            Login login = loginService.getLogin(loginDetails.getEmail());

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
