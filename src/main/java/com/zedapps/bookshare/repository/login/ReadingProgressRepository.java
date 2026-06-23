package com.zedapps.bookshare.repository.login;

import com.zedapps.bookshare.entity.login.ReadingProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author smzoha
 * @since 23/1/26
 **/
@Repository
public interface ReadingProgressRepository extends JpaRepository<ReadingProgress, Long> {

    @Query("FROM ReadingProgress WHERE user.email = :userEmail AND YEAR(endDate) = :endDateYear AND completed = true")
    List<ReadingProgress> findReadingProgressesByUser_EmailAndEndDateYear(String userEmail, int endDateYear);

    @Query("SELECT DISTINCT YEAR(endDate) FROM ReadingProgress WHERE user.email = :userEmail AND endDate IS NOT NULL AND completed = true")
    List<Integer> findReadingProgressYearsByUser_Email(String userEmail);
}
