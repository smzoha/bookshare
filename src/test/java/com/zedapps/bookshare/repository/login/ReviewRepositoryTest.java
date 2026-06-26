package com.zedapps.bookshare.repository.login;

import com.zedapps.bookshare.entity.book.Author;
import com.zedapps.bookshare.entity.book.Book;
import com.zedapps.bookshare.entity.login.Login;
import com.zedapps.bookshare.entity.login.Review;
import com.zedapps.bookshare.enums.Status;
import com.zedapps.bookshare.repository.book.AuthorRepository;
import com.zedapps.bookshare.repository.book.BookRepository;
import com.zedapps.bookshare.util.TestUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author smzoha
 * @since 1/5/26
 **/
@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ReviewRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17-alpine");

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private LoginRepository loginRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    private Book book;
    private Book reviewedBook;

    private Login login;

    @BeforeAll
    void setUp() throws InterruptedException {
        Author author = TestUtils.getAuthor("Test", "Author");
        authorRepository.saveAndFlush(author);

        login = TestUtils.getLogin("user@test.com", "user", true);
        loginRepository.saveAndFlush(login);

        book = TestUtils.getBook("Test Book", "9780451524935", author, Status.ACTIVE);
        reviewedBook = TestUtils.getBook("Reviewed Book", "9780679783411", author, Status.ACTIVE);

        bookRepository.saveAllAndFlush(List.of(book, reviewedBook));

        for (int i = 0; i < 5; i++) {
            Review review = TestUtils.getReview(reviewedBook, login, 5);
            reviewRepository.saveAndFlush(review);

            Thread.sleep(100);
        }
    }

    @Test
    void findReviewsByBookOrderByReviewDateDesc_returnPagedReviews() {
        Pageable pageable = PageRequest.of(0, 3);
        Page<Review> reviews = reviewRepository.findReviewsByBookOrderByReviewDateDesc(reviewedBook, pageable);

        assertFalse(reviews.isEmpty());
        assertEquals(5, reviews.getTotalElements());
        assertEquals(2, reviews.getTotalPages());
        assertEquals(3, reviews.getSize());
        assertEquals(0, reviews.getNumber());

        Page<Review> emptyReviews = reviewRepository.findReviewsByBookOrderByReviewDateDesc(book, pageable);
        assertTrue(emptyReviews.isEmpty());
    }

    @Test
    void findReviewsByBookOrderByReviewDateDesc_verifyOrdering() {
        Pageable pageable = PageRequest.of(0, 3);
        Page<Review> reviews = reviewRepository.findReviewsByBookOrderByReviewDateDesc(reviewedBook, pageable);

        Review latestReview = reviews.getContent().getFirst();
        Review oldestReview = reviews.getContent().getLast();

        assertTrue(latestReview.getReviewDate().isAfter(oldestReview.getReviewDate()));
    }

    @Test
    void findReviewsByUser_EmailAndReviewDateYear_filtersByUserAndYear() {
        int currentYear = LocalDate.now().getYear();
        int previousYear = currentYear - 1;

        List<Review> currentYearReviews = reviewRepository.findReviewsByUser_EmailAndReviewDateYear(login.getEmail(), currentYear);
        assertEquals(5, currentYearReviews.size());

        List<Review> prevYearReviews = reviewRepository.findReviewsByUser_EmailAndReviewDateYear(login.getEmail(), previousYear);
        assertTrue(prevYearReviews.isEmpty());

        List<Review> otherUserReviews = reviewRepository.findReviewsByUser_EmailAndReviewDateYear("other@test.com", currentYear);
        assertTrue(otherUserReviews.isEmpty());
    }
}
