package com.zedapps.bookshare.repository.activity;

import com.zedapps.bookshare.entity.activity.ActivityOutbox;
import com.zedapps.bookshare.enums.ActivityStatus;
import com.zedapps.bookshare.util.TestUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
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
public class ActivityOutboxRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    @Autowired
    private ActivityOutboxRepository activityOutboxRepository;

    @BeforeAll
    void setupActivityOutboxList() {
        List<ActivityOutbox> activityOutboxList = new ArrayList<>();

        for (int i = 0; i < 150; i++) {
            ActivityOutbox activityOutbox = TestUtils.getActivityOutboxItem(ActivityStatus.PENDING);
            activityOutboxList.add(activityOutbox);
        }

        for (int i = 0; i < 25; i++) {
            ActivityOutbox activityOutbox = TestUtils.getActivityOutboxItem(ActivityStatus.FAILED);
            activityOutboxList.add(activityOutbox);
        }

        activityOutboxRepository.saveAllAndFlush(activityOutboxList);
    }

    @Test
    void findTop100ByStatusOrderByCreatedAt_returnList() {
        List<ActivityOutbox> pendingOutboxList = activityOutboxRepository.findTop100ByStatusOrderByCreatedAt(
                ActivityStatus.PENDING);

        assertFalse(pendingOutboxList.isEmpty());
        assertEquals(100, pendingOutboxList.size());
        assertEquals(ActivityStatus.PENDING, pendingOutboxList.getFirst().getStatus());

        List<ActivityOutbox> failedOutboxList = activityOutboxRepository.findTop100ByStatusOrderByCreatedAt(
                ActivityStatus.FAILED);

        assertFalse(failedOutboxList.isEmpty());
        assertEquals(25, failedOutboxList.size());
        assertEquals(ActivityStatus.FAILED, failedOutboxList.getFirst().getStatus());

        List<ActivityOutbox> completedOutboxList = activityOutboxRepository.findTop100ByStatusOrderByCreatedAt(
                ActivityStatus.COMPLETED);

        assertTrue(completedOutboxList.isEmpty());
    }

    @Test
    void findByStatusIn_returnList() {
        List<ActivityOutbox> outboxList = activityOutboxRepository.findByStatusIn(List.of(ActivityStatus.PENDING));

        boolean hasPending = outboxList.stream().anyMatch(o -> o.getStatus() == ActivityStatus.PENDING);
        boolean hasFailed = outboxList.stream().anyMatch(o -> o.getStatus() == ActivityStatus.FAILED);

        assertTrue(hasPending);
        assertFalse(hasFailed);

        outboxList = activityOutboxRepository.findByStatusIn(List.of(ActivityStatus.PENDING, ActivityStatus.FAILED));

        hasPending = outboxList.stream().anyMatch(o -> o.getStatus() == ActivityStatus.PENDING);
        hasFailed = outboxList.stream().anyMatch(o -> o.getStatus() == ActivityStatus.FAILED);

        assertTrue(hasPending);
        assertTrue(hasFailed);

        List<ActivityOutbox> emptyOutboxList = activityOutboxRepository.findByStatusIn(List.of(ActivityStatus.COMPLETED));
        assertTrue(emptyOutboxList.isEmpty());
    }
}
