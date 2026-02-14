package com.zedapps.bookshare.service.book;

import com.zedapps.bookshare.dto.login.LoginDetails;
import com.zedapps.bookshare.entity.activity.enums.ActivityType;
import com.zedapps.bookshare.entity.book.Author;
import com.zedapps.bookshare.entity.book.Book;
import com.zedapps.bookshare.entity.login.Login;
import com.zedapps.bookshare.repository.book.BookRepository;
import com.zedapps.bookshare.repository.login.AuthorRepository;
import com.zedapps.bookshare.service.activity.ActivityService;
import jakarta.persistence.NoResultException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author smzoha
 * @since 14/2/26
 **/
@Service
public class BookAdminService {

    private final AuthorRepository authorRepository;
    private final BookRepository bookRepository;
    private final ActivityService activityService;

    public BookAdminService(AuthorRepository authorRepository,
                            BookRepository bookRepository,
                            ActivityService activityService) {

        this.authorRepository = authorRepository;
        this.bookRepository = bookRepository;
        this.activityService = activityService;
    }

    public List<Book> getBookList() {
        return bookRepository.findAll();
    }

    public Book getBook(Long bookId) {
        return bookRepository.findBookById(bookId).orElseThrow(NoResultException::new);
    }

    public List<Author> getAuthorList() {
        return authorRepository.findAll();
    }

    public Author getAuthor(Long id) {
        return authorRepository.findById(id).orElseThrow(NoResultException::new);
    }

    public Optional<Author> getAuthorByLogin(Login login) {
        return authorRepository.findAuthorByLogin(login);
    }

    @Transactional
    public Book saveBook(Book book) {
        boolean isNew = book.getId() == null;

        book = bookRepository.save(book);

        LoginDetails loginDetails = (LoginDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        activityService.saveActivityOutbox(isNew ? ActivityType.BOOK_ADD : ActivityType.BOOK_UPDATE,
                book.getId(),
                Map.of(
                        "actionBy", loginDetails.getEmail(),
                        "affectedBookId", book.getId()
                ));

        return book;
    }

    @Transactional
    public Author saveAuthor(Author author) {
        boolean isNew = author.getId() == null;

        author = authorRepository.save(author);

        LoginDetails loginDetails = (LoginDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        activityService.saveActivityOutbox(isNew ? ActivityType.AUTHOR_ADD : ActivityType.AUTHOR_UPDATE,
                author.getId(),
                Map.of(
                        "actionBy", loginDetails.getEmail(),
                        "affectedAuthorId", author.getId(),
                        "affectedAuthorLogin", author.getLogin() != null ? author.getLogin().getId() : ""
                ));

        return author;
    }
}
