package com.zedapps.bookshare.repository.feed;

import com.zedapps.bookshare.entity.activity.Activity;
import com.zedapps.bookshare.entity.feed.FeedEntry;
import com.zedapps.bookshare.entity.login.Login;
import com.zedapps.bookshare.enums.ActivityType;
import com.zedapps.bookshare.repository.activity.ActivityRepository;
import com.zedapps.bookshare.repository.login.LoginRepository;
import com.zedapps.bookshare.util.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author smzoha
 * @since 1/5/26
 **/
@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class FeedEntryRepositoryTest {

    private static final String INSERT_SQL = "INSERT INTO feed_entry VALUES (nextval('feed_entry_seq'), ?, ?, ?)";

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17-alpine");

    @Autowired
    private LoginRepository loginRepository;

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private FeedEntryRepository feedEntryRepository;

    private Login audience1;
    private Login audience2;
    private Login audience3;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setup() {
        audience1 = TestUtils.getLogin("user1@test.com", "user1", true);
        audience2 = TestUtils.getLogin("user2@test.com", "user2", true);
        audience3 = TestUtils.getLogin("user3@test.com", "user3", true);

        loginRepository.saveAllAndFlush(List.of(audience1, audience2, audience3));

        LocalDateTime activityDateTime = LocalDateTime.now().minusDays(30);

        /*
         For audience1 and audience2, the activity date/time will start 30 days prior to the current date.
         Each activity's `createdAt` date will be incremented by 1 day.
         As such, if searched for 15 days, 15 entries will be returned; for 30, 30 entries, and so forth.
         JDBC Template is used here to avoid @CreationTimestamp annotations default behavior for `createdAt`
         */
        for (int i = 0; i <= 30; i++) {
            Activity activity = TestUtils.getActivity(ActivityType.ADD_FRIEND);
            activityRepository.saveAndFlush(activity);

            for (Login login : List.of(audience1, audience2)) {
                jdbcTemplate.update(INSERT_SQL, login.getId(), activity.getId(), activityDateTime);
            }

            activityDateTime = activityDateTime.plusDays(1);
        }
    }

    @Test
    void getPagedFeedEntries_returnPagedFeedEntries() {
        LocalDateTime cutoffDateTime = LocalDateTime.now().minusDays(15);
        Pageable pageable = PageRequest.of(0, 5);

        Page<FeedEntry> audience1Feed = feedEntryRepository.getPagedFeedEntries(audience1, cutoffDateTime, pageable);

        assertFalse(audience1Feed.isEmpty());
        assertEquals(15, audience1Feed.getTotalElements());
        assertEquals(3, audience1Feed.getTotalPages());
        assertEquals(0, audience1Feed.getNumber());

        assertTrue(audience1Feed.getContent().getFirst().getCreatedAt()
                .isAfter(audience1Feed.getContent().getLast().getCreatedAt()));

        cutoffDateTime = LocalDateTime.now().minusDays(30);
        Page<FeedEntry> audience2Feed = feedEntryRepository.getPagedFeedEntries(audience2, cutoffDateTime, pageable);

        assertFalse(audience2Feed.isEmpty());
        assertEquals(30, audience2Feed.getTotalElements());
        assertEquals(6, audience2Feed.getTotalPages());
        assertEquals(0, audience2Feed.getNumber());

        assertTrue(audience2Feed.getContent().getFirst().getCreatedAt()
                .isAfter(audience2Feed.getContent().getLast().getCreatedAt()));

        Page<FeedEntry> audience3Feed = feedEntryRepository.getPagedFeedEntries(audience3, cutoffDateTime, pageable);
        assertTrue(audience3Feed.isEmpty());
    }
}
