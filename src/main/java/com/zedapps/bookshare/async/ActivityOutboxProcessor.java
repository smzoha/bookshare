package com.zedapps.bookshare.async;

import com.zedapps.bookshare.entity.activity.Activity;
import com.zedapps.bookshare.entity.activity.ActivityOutbox;
import com.zedapps.bookshare.entity.login.Login;
import com.zedapps.bookshare.enums.ActivityStatus;
import com.zedapps.bookshare.enums.ActivityType;
import com.zedapps.bookshare.service.activity.ActivityService;
import com.zedapps.bookshare.service.login.LoginService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author smzoha
 * @since 13/2/26
 **/
@Slf4j
@Component
@RequiredArgsConstructor
public class ActivityOutboxProcessor {

    private final ActivityService activityService;
    private final LoginService loginService;

    @Transactional
    @Scheduled(fixedDelay = 15 * 1000)
    public void processOutbox() {
        List<ActivityOutbox> unprocessedActivityOutboxItems = activityService.getUnprocessedActivityOutboxItems();

        if (CollectionUtils.isEmpty(unprocessedActivityOutboxItems)) {
            return;
        }

        List<Activity> processedActivityList = new ArrayList<>();

        for (ActivityOutbox activityOutbox : unprocessedActivityOutboxItems) {
            try {
                Login login = null;

                if (activityOutbox.getPayload().containsKey("actionBy")) {
                    login = loginService.getLogin(activityOutbox.getPayload().get("actionBy").toString());
                }

                Activity activity = Activity.builder()
                        .login(login)
                        .eventType(activityOutbox.getEventType())
                        .referenceEntity(activityOutbox.getReferenceEntity())
                        .referenceId(activityOutbox.getReferenceId())
                        .metadata(activityOutbox.getPayload())
                        .internal(!ActivityType.FEED_ACTIVITIES.contains(activityOutbox.getEventType()))
                        .build();

                processedActivityList.add(activity);
                activityOutbox.setStatus(ActivityStatus.COMPLETED);

            } catch (Exception e) {
                log.error("Error processing outbox activity: id={}", activityOutbox.getId(), e);

                if (activityOutbox.getRetryCount() >= 3) {
                    log.error("Retry count exceeded. Marking Outbox Activity as Failed: id={}", activityOutbox.getId());
                    activityOutbox.setStatus(ActivityStatus.FAILED);

                } else {
                    activityOutbox.setRetryCount(activityOutbox.getRetryCount() + 1);
                    activityOutbox.setStatus(ActivityStatus.PENDING);
                }

            } finally {
                activityOutbox.setProcessedAt(LocalDateTime.now());
            }
        }

        activityService.processOutboxActivity(unprocessedActivityOutboxItems, processedActivityList);
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void cleanupStaleOutboxActivity() {
        List<ActivityOutbox> staleActivityOutboxList = activityService.getProcessedActivityOutboxItems();

        try {
            activityService.deleteOutboxActivityList(staleActivityOutboxList);
            log.debug("Successfully cleaned up stale ActivityOutbox data");

        } catch (Exception e) {
            log.error("Error cleaning up stale ActivityOutbox data");
        }
    }
}
