package com.zedapps.bookshare.async;

import com.zedapps.bookshare.dto.activity.ActivityFeedDto;
import com.zedapps.bookshare.entity.activity.Activity;
import com.zedapps.bookshare.entity.feed.FeedEntry;
import com.zedapps.bookshare.entity.login.Connection;
import com.zedapps.bookshare.repository.connection.ConnectionRepository;
import com.zedapps.bookshare.repository.feed.FeedEntryRepository;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

/**
 * @author smzoha
 * @since 12/3/26
 **/
@Component
@AllArgsConstructor
public class FeedEntryListener {

    private final ConnectionRepository connectionRepository;
    private final FeedEntryRepository feedEntryRepository;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onActivityPublish(ActivityFeedDto activityFeedDto) {
        Activity activity = activityFeedDto.activity();
        List<Connection> connections = connectionRepository.findConnectionsByPerson1(activityFeedDto.login());

        List<FeedEntry> feedEntries = connections.stream()
                .map(connection -> new FeedEntry(connection.getPerson2(), activity))
                .toList();

        feedEntryRepository.saveAll(feedEntries);
    }
}
