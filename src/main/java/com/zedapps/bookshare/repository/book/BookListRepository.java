package com.zedapps.bookshare.repository.book;

import com.zedapps.bookshare.entity.book.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

/**
 * @author smzoha
 * @since 15/11/25
 **/
@Repository
public interface BookListRepository extends PagingAndSortingRepository<Book, Long> {

    @Query("""
            SELECT b
            FROM Book b
            LEFT JOIN b.reviews r
            LEFT JOIN b.genres g
            LEFT JOIN b.tags t
            LEFT JOIN b.authors a
            WHERE (:genre IS NULL OR g.id IN :genre)
            AND (:tag IS NULL OR t.id IN :tag)
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
    Page<Book> getPaginatedBooks(Pageable pageable, String rating, String genre, String tag, String sortAttr, String sortDirection);
}
