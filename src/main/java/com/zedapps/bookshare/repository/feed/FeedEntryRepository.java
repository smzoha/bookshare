package com.zedapps.bookshare.repository.feed;

import com.zedapps.bookshare.entity.feed.FeedEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author smzoha
 * @since 12/3/26
 **/
@Repository
public interface FeedEntryRepository extends JpaRepository<FeedEntry, Long> {
}
