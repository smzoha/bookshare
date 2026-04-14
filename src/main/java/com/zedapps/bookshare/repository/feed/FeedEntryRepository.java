package com.zedapps.bookshare.repository.feed;

import com.zedapps.bookshare.entity.feed.FeedEntry;
import com.zedapps.bookshare.entity.login.Login;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

/**
 * @author smzoha
 * @since 12/3/26
 **/
@Repository
public interface FeedEntryRepository extends JpaRepository<FeedEntry, Long> {

    @EntityGraph("feedEntry.withActivity")
    @Query("FROM FeedEntry WHERE audienceLogin = :audience AND createdAt >= :timestamp ORDER BY createdAt DESC")
    Page<FeedEntry> getPagedFeedEntries(Login audience, LocalDateTime timestamp, Pageable pageable);
}
