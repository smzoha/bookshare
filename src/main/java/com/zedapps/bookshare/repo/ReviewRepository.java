package com.zedapps.bookshare.repo;

import com.zedapps.bookshare.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author smzoha
 * @since 31/1/25
 **/
@Repository
public interface ReviewRepository extends JpaRepository<Review, Integer> {
}
