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
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

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

    @Cacheable(cacheNames = "book-lists", key = "'all'")
    public List<Book> getBookList() {
        return bookRepository.findAll();
    }

    @Cacheable(cacheNames = "books", key = "#bookId")
    public Book getBook(Long bookId) {
        return bookRepository.findBookById(bookId).orElseThrow(NoResultException::new);
    }

    @Cacheable(cacheNames = "genre-lists", key = "'all'")
    public List<Genre> getGenreList() {
        return genreRepository.findAll().stream()
                .sorted(Comparator.comparing(Genre::getName))
                .toList();
    }

    @Cacheable(cacheNames = "genres", key = "#id")
    public Genre getGenre(Long id) {
        return genreRepository.findById(id).orElseThrow(NoResultException::new);
    }

    @Cacheable(cacheNames = "genres", key = "#name", unless = "#result == null || #result.isEmpty()")
    public Optional<Genre> getGenreByName(String name) {
        return genreRepository.findGenreByName(name);
    }

    @Cacheable(cacheNames = "tag-lists", key = "'all'")
    public List<Tag> getTagList() {
        return tagRepository.findAll().stream()
                .sorted(Comparator.comparing(Tag::getName))
                .toList();
    }

    @Cacheable(cacheNames = "tags", key = "#id")
    public Tag getTag(Long id) {
        return tagRepository.findById(id).orElseThrow(NoResultException::new);
    }

    @Cacheable(cacheNames = "tags", key = "#name", unless = "#result == null || #result.isEmpty()")
    public Optional<Tag> getTagByName(String name) {
        return tagRepository.findTagByName(name);
    }

    @Cacheable(cacheNames = "author-lists", key = "'all'")
    public List<Author> getAuthorList() {
        return authorRepository.findAll();
    }

    @Cacheable(cacheNames = "authors", key = "#id")
    public Author getAuthor(Long id) {
        return authorRepository.findById(id).orElseThrow(NoResultException::new);
    }

    @Cacheable(cacheNames = "authors", key = "'login-' + #login.id", unless = "#result == null || #result.isEmpty()")
    public Optional<Author> getAuthorByLogin(Login login) {
        return authorRepository.findAuthorByLogin(login);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = "book-lists", allEntries = true),
            @CacheEvict(cacheNames = "books", key = "#book.id", condition = "#book.id != null")
    })
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
    @Caching(evict = {
            @CacheEvict(cacheNames = "genre-lists", allEntries = true),
            @CacheEvict(cacheNames = "genres", key = "#genre.id", condition = "#genre.id != null")
    })
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
    @Caching(evict = {
            @CacheEvict(cacheNames = "tag-lists", allEntries = true),
            @CacheEvict(cacheNames = "tags", key = "#tag.id", condition = "#tag.id != null")
    })
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
    @Caching(evict = {
            @CacheEvict(cacheNames = "author-lists", allEntries = true),
            @CacheEvict(cacheNames = "authors", key = "#author.id", condition = "#author.id != null"),
            @CacheEvict(cacheNames = "authors", key = "'login-' + #author.login.id",
                    condition = "#author.id != null && #author.login != null")
    })
    public void saveAuthor(Author author) {
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
    }
}
