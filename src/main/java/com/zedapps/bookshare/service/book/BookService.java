package com.zedapps.bookshare.service.book;

import com.zedapps.bookshare.entity.book.Book;
import com.zedapps.bookshare.entity.login.Review;
import com.zedapps.bookshare.repository.book.BookRepository;
import com.zedapps.bookshare.repository.book.ReviewRepository;
import jakarta.persistence.NoResultException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author smzoha
 * @since 15/9/25
 **/
@Service
public class BookService {

    private final BookRepository bookRepository;
    private final ReviewRepository reviewRepository;

    public BookService(BookRepository bookRepository,
                       ReviewRepository reviewRepository) {

        this.bookRepository = bookRepository;
        this.reviewRepository = reviewRepository;
    }

    public Book getBook(Long bookId) {
        return bookRepository.findBookById(bookId).orElseThrow(NoResultException::new);
    }

    public List<Book> getRelatedBooks(Book book) {
        List<Book> relatedBooks = bookRepository.getRelatedBooks(book.getGenres(), book.getTags());
        relatedBooks.remove(book);

        return relatedBooks;
    }

    public Page<Review> getReviewsByBook(Book book, int pageNumber) {
        return reviewRepository.findReviewsByBookOrderByReviewDateDesc(book, PageRequest.of(pageNumber, 5));
    }
}
