package com.zedapps.bookshare.repository.login;

import com.zedapps.bookshare.entity.login.ShelvedBook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author smzoha
 * @since 6/1/26
 **/
@Repository
public interface ShelvedBookRepository extends JpaRepository<ShelvedBook, Long> {
}
