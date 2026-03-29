package com.zedapps.bookshare.service.book;

import com.zedapps.bookshare.dto.login.LoginDetails;
import com.zedapps.bookshare.entity.book.Author;
import com.zedapps.bookshare.entity.book.Book;
import com.zedapps.bookshare.entity.book.Genre;
import com.zedapps.bookshare.entity.book.Tag;
import com.zedapps.bookshare.entity.login.Login;
import com.zedapps.bookshare.enums.ActivityType;
import com.zedapps.bookshare.repository.book.BookRepository;
import com.zedapps.bookshare.repository.book.GenreRepository;
import com.zedapps.bookshare.repository.book.TagRepository;
import com.zedapps.bookshare.repository.login.AuthorRepository;
import com.zedapps.bookshare.service.activity.ActivityService;
import jakarta.persistence.NoResultException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * @author smzoha
 * @since 14/2/26
 **/
@Service
@RequiredArgsConstructor
public class BookAdminService {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final GenreRepository genreRepository;
    private final TagRepository tagRepository;
    private final ActivityService activityService;

    public List<Book> getBookList() {
        return bookRepository.findAll();
    }

    public Book getBook(Long bookId) {
        return bookRepository.findBookById(bookId).orElseThrow(NoResultException::new);
    }

    public List<Genre> getGenreList() {
        return genreRepository.findAll();
    }

    public Genre getGenre(Long id) {
        return genreRepository.findById(id).orElseThrow(NoResultException::new);
    }

    public Optional<Genre> getGenreByName(String name) {
        return genreRepository.findGenreByName(name);
    }

    public List<Tag> getTagList() {
        return tagRepository.findAll();
    }

    public Tag getTag(Long id) {
        return tagRepository.findById(id).orElseThrow(NoResultException::new);
    }

    public Optional<Tag> getTagByName(String name) {
        return tagRepository.findTagByName(name);
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
    public void saveBook(Book book, ActivityType activityType) {
        boolean isNew = book.getId() == null;

        book = bookRepository.save(book);

        activityType = Objects.nonNull(activityType)
                ? activityType
                : isNew ? ActivityType.BOOK_ADD : ActivityType.BOOK_UPDATE;

        LoginDetails loginDetails = (LoginDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        activityService.saveActivityOutbox(activityType,
                book.getId(),
                Map.of(
                        "actionBy", loginDetails.getEmail(),
                        "affectedBookId", book.getId(),
                        "bookName", book.getTitle(),
                        "bookIsbn", book.getIsbn()
                ));
    }

    @Transactional
    public void saveGenre(Genre genre) {
        boolean isNew = genre.getId() == null;

        genre = genreRepository.save(genre);

        LoginDetails loginDetails = (LoginDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        activityService.saveActivityOutbox(isNew ? ActivityType.GENRE_ADD : ActivityType.GENRE_UPDATE,
                genre.getId(),
                Map.of(
                        "actionBy", loginDetails.getEmail(),
                        "affectedGenreId", genre.getId(),
                        "genreName", genre.getName()
                ));
    }

    @Transactional
    public void saveTag(Tag tag) {
        boolean isNew = tag.getId() == null;

        tag = tagRepository.save(tag);

        LoginDetails loginDetails = (LoginDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        activityService.saveActivityOutbox(isNew ? ActivityType.TAG_ADD : ActivityType.TAG_UPDATE,
                tag.getId(),
                Map.of(
                        "actionBy", loginDetails.getEmail(),
                        "affectedTagId", tag.getId(),
                        "tagName", tag.getName()
                ));
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
                        "affectedAuthorLogin", author.getLogin() != null ? author.getLogin().getId() : "",
                        "authorFirstName", author.getFirstName(),
                        "authorLastName", author.getLastName()
                ));

        return author;
    }
}
