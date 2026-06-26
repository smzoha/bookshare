package com.zedapps.bookshare.repository.login;

import com.zedapps.bookshare.entity.book.Book;
import com.zedapps.bookshare.entity.login.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author smzoha
 * @since 22/9/25
 **/
@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    Page<Review> findReviewsByBookOrderByReviewDateDesc(Book book, Pageable pageable);

    @Query("FROM Review WHERE user.email = :userEmail AND YEAR(reviewDate) = :reviewDateYear")
    List<Review> findReviewsByUser_EmailAndReviewDateYear(String userEmail, int reviewDateYear);
}
