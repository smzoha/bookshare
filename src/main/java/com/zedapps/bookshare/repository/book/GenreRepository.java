package com.zedapps.bookshare.repository.book;

import com.zedapps.bookshare.entity.book.Genre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author smzoha
 * @since 13/9/25
 **/
@Repository
public interface GenreRepository extends JpaRepository<Genre, Long> {
}
