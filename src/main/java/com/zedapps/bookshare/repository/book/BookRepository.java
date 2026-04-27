package com.zedapps.bookshare.repository.book;

import com.zedapps.bookshare.entity.book.Book;
import com.zedapps.bookshare.entity.book.Genre;
import com.zedapps.bookshare.entity.book.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

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

    @EntityGraph("book.withAssociations")
    @Query("""
            SELECT b
            FROM Book b
            LEFT JOIN b.genres g
            LEFT JOIN b.tags t
            WHERE g IN (:genres) OR t IN (:tags)
            """)
    List<Book> getRelatedBooks(Set<Genre> genres, Set<Tag> tags);

    @Query("""
            SELECT b
            FROM Book b
            LEFT JOIN b.reviews r
            LEFT JOIN b.genres g
            LEFT JOIN b.tags t
            LEFT JOIN b.authors a
            WHERE b.status = 'ACTIVE'
            AND (:genre IS NULL OR g.id IN :genre)
            AND (:tag IS NULL OR t.id IN :tag)
            AND (:query IS NULL OR b.isbn LIKE :query OR LOWER(b.title) LIKE :query)
            GROUP BY b
            HAVING (:rating IS NULL OR COALESCE(AVG(r.rating), 0) >= :rating)
            ORDER BY
                CASE WHEN :sortAttr = 'author' AND :sortDirection = 'asc' THEN MIN(a.firstName) END ASC,
                    CASE WHEN :sortAttr = 'author' AND :sortDirection = 'desc' THEN MIN(a.firstName) END DESC,
                CASE WHEN :sortAttr = 'rating' AND :sortDirection = 'asc' THEN COALESCE(AVG(r.rating), 0) END ASC,
                CASE WHEN :sortAttr = 'rating' AND :sortDirection = 'desc' THEN COALESCE(AVG(r.rating), 0) END DESC,
                CASE WHEN :sortAttr = 'title' AND :sortDirection = 'asc' THEN b.title END ASC,
                CASE WHEN :sortAttr = 'title' AND :sortDirection = 'desc' THEN b.title END DESC,
                b.title ASC
            """)
    Page<Book> getPaginatedBooks(Pageable pageable, String query, String rating, String genre, String tag, String sortAttr, String sortDirection);

    @EntityGraph("book.withAll")
    Optional<Book> findBookById(Long id);

    @EntityGraph("book.withAssociations")
    List<Book> findAll();
}
