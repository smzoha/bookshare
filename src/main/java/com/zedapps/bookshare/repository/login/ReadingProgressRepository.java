package com.zedapps.bookshare.repository.login;

import com.zedapps.bookshare.entity.login.ReadingProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author smzoha
 * @since 23/1/26
 **/
@Repository
public interface ReadingProgressRepository extends JpaRepository<ReadingProgress, Long> {
}
