package com.zedapps.bookshare.service.book;

import com.zedapps.bookshare.dto.book.BookReviewDto;
import com.zedapps.bookshare.dto.book.ReviewLikeResponseDto;
import com.zedapps.bookshare.dto.login.LoginDetails;
import com.zedapps.bookshare.entity.book.Book;
import com.zedapps.bookshare.entity.login.Login;
import com.zedapps.bookshare.entity.login.Review;
import com.zedapps.bookshare.repository.book.BookListRepository;
import com.zedapps.bookshare.repository.book.BookRepository;
import com.zedapps.bookshare.repository.book.ReviewRepository;
import com.zedapps.bookshare.service.login.LoginService;
import jakarta.persistence.NoResultException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;

import java.util.List;
import java.util.Objects;

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

    public BookService(BookRepository bookRepository,
                       BookListRepository bookListRepository,
                       ReviewRepository reviewRepository,
                       LoginService loginService) {

        this.bookRepository = bookRepository;
        this.bookListRepository = bookListRepository;
        this.reviewRepository = reviewRepository;
        this.loginService = loginService;
    }

    public Book getBook(Long bookId) {
        return bookRepository.findBookById(bookId).orElseThrow(NoResultException::new);
    }

    public Page<Book> getPaginatedBooks(int page) {
        Pageable pageable = PageRequest.of(page, 18, Sort.by("title").ascending());

        return bookListRepository.findAll(pageable);
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

        if (Objects.nonNull(loginDetails)) {
            Login login = loginService.getLogin(loginDetails.getEmail());
            model.put("shelves", login.getShelves());
        }

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

    private Review createReviewFromDto(BookReviewDto reviewDto, Book book, Login login) {
        Review review = new Review();
        review.setRating(reviewDto.getRating());
        review.setContent(reviewDto.getContent());
        review.setBook(book);
        review.setUser(login);

        return review;
    }
}
