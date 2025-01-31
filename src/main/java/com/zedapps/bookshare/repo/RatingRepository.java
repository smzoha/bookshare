package com.zedapps.bookshare.repo;

import com.zedapps.bookshare.entity.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author smzoha
 * @since 31/1/25
 **/
@Repository
public interface RatingRepository extends JpaRepository<Rating, Integer> {
}
