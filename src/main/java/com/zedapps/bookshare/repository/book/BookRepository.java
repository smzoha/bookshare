package com.zedapps.bookshare.repository.book;

import com.zedapps.bookshare.entity.book.Book;
import com.zedapps.bookshare.entity.book.Genre;
import com.zedapps.bookshare.entity.book.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * @author smzoha
 * @since 13/9/25
 **/
@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    @Query("""
            SELECT b
            FROM Book b
            LEFT JOIN Review r ON r.book.id = b.id
            WHERE b.status = 'ACTIVE'
            GROUP BY b
            ORDER BY AVG(r.rating), b.updatedAt DESC
            FETCH FIRST 10 ROWS ONLY
            """)
    List<Book> getFeaturedBooks();

    @Query("""
            SELECT b
            FROM Book b
            LEFT JOIN b.genres g
            LEFT JOIN b.tags t
            WHERE g IN (:genres) OR t IN (:tags)
            """)
    List<Book> getRelatedBooks(List<Genre> genres, List<Tag> tags);

    Optional<Book> findBookById(Long id);
}
