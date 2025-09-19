package com.zedapps.bookshare.service.book;

import com.zedapps.bookshare.entity.book.Book;
import com.zedapps.bookshare.repository.book.BookRepository;
import jakarta.persistence.NoResultException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * @author smzoha
 * @since 15/9/25
 **/
@Service
public class BookService {

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public Book getBook(Long bookId) {
        return bookRepository.findBookById(bookId).orElseThrow(NoResultException::new);
    }

    public List<Book> getRelatedBooks(Book book) {
        List<Book> relatedBooks = bookRepository.getRelatedBooks(book.getGenres(), book.getTags());
        relatedBooks.remove(book);

        return relatedBooks;
    }
}
