package com.zedapps.bookshare.service.book;

import com.zedapps.bookshare.dto.login.LoginDetails;
import com.zedapps.bookshare.entity.activity.enums.ActivityType;
import com.zedapps.bookshare.entity.book.Author;
import com.zedapps.bookshare.entity.login.Login;
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
public class AuthorService {

    private final AuthorRepository authorRepository;
    private final ActivityService activityService;

    public AuthorService(AuthorRepository authorRepository, ActivityService activityService) {
        this.authorRepository = authorRepository;
        this.activityService = activityService;
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
