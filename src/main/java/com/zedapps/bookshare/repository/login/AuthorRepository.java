package com.zedapps.bookshare.repository.login;

import com.zedapps.bookshare.entity.book.Author;
import com.zedapps.bookshare.entity.login.Login;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * @author smzoha
 * @since 21/10/25
 **/
@Repository
public interface AuthorRepository extends JpaRepository<Author, Long> {

    @EntityGraph("author.withLogin")
    Optional<Author> findById(Long id);

    @EntityGraph("author.withLogin")
    List<Author> findAll();

    @EntityGraph("author.withLogin")
    Optional<Author> findAuthorByLogin(Login login);
}
