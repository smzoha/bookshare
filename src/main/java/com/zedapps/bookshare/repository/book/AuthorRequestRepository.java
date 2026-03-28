package com.zedapps.bookshare.repository.book;

import com.zedapps.bookshare.entity.book.AuthorRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @author smzoha
 * @since 28/3/26
 **/
@Repository
public interface AuthorRequestRepository extends JpaRepository<AuthorRequest, Long> {

    Optional<AuthorRequest> getAuthorRequestsByLoginEmail(String loginEmail);
}
