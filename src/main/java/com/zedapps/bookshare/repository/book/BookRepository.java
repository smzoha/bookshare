package com.zedapps.bookshare.repository.book;

import com.zedapps.bookshare.entity.book.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

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
}
