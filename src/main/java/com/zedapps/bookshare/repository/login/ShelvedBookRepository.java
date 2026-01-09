package com.zedapps.bookshare.repository.login;

import com.zedapps.bookshare.entity.book.Book;
import com.zedapps.bookshare.entity.login.Login;
import com.zedapps.bookshare.entity.login.Shelf;
import com.zedapps.bookshare.entity.login.ShelvedBook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @author smzoha
 * @since 6/1/26
 **/
@Repository
public interface ShelvedBookRepository extends JpaRepository<ShelvedBook, Long> {

    Optional<ShelvedBook> findShelvedBookByLoginAndShelfAndBook(Login login, Shelf shelf, Book book);
}
