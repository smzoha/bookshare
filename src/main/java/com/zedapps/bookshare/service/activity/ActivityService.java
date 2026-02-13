package com.zedapps.bookshare.service.activity;

import com.zedapps.bookshare.entity.activity.Activity;
import com.zedapps.bookshare.entity.activity.ActivityOutbox;
import com.zedapps.bookshare.entity.activity.enums.ActivityStatus;
import com.zedapps.bookshare.entity.activity.enums.ActivityType;
import com.zedapps.bookshare.entity.login.Login;
import com.zedapps.bookshare.repository.activity.ActivityOutboxRepository;
import com.zedapps.bookshare.repository.activity.ActivityRepository;
import com.zedapps.bookshare.service.login.LoginService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * @author smzoha
 * @since 13/2/26
 **/
@Slf4j
@Service
public class ActivityService {

    private final ActivityOutboxRepository activityOutboxRepository;
    private final ActivityRepository activityRepository;
    private final LoginService loginService;

    public ActivityService(ActivityOutboxRepository activityOutboxRepository,
                           ActivityRepository activityRepository,
                           LoginService loginService) {

        this.activityOutboxRepository = activityOutboxRepository;
        this.activityRepository = activityRepository;
        this.loginService = loginService;
    }

    @Transactional
    public void saveActivityOutbox(ActivityType type,
                                   Long referenceId,
                                   Map<String, Object> payload) {

        ActivityOutbox activityOutbox = ActivityOutbox.builder()
                .eventType(type)
                .referenceEntity(type.getReferenceEntity())
                .referenceId(referenceId)
                .payload(payload)
                .status(ActivityStatus.PENDING)
                .build();

        try {
            activityOutboxRepository.save(activityOutbox);

        } catch (Exception e) {
            log.error("Error publishing activity to outbox: {}, {}", activityOutbox.getEventType().name(),
                    activityOutbox.getPayload().getOrDefault("actionBy", ""));
        }
    }

    @Transactional
    public void saveActivity(ActivityType type,
                             String loginEmail,
                             Long referenceId,
                             Map<String, Object> metadata) {

        Login login = loginService.getLogin(metadata.getOrDefault("actionBy", loginEmail).toString());

        Activity activity = Activity.builder()
                .login(login)
                .eventType(type)
                .referenceEntity(type.getReferenceEntity())
                .referenceId(referenceId)
                .metadata(metadata)
                .internal(!ActivityType.FEED_ACTIVITIES.contains(type))
                .build();

        saveActivity(activity);
    }

    @Transactional
    public void saveActivity(Activity activity) {
        try {
            activityRepository.save(activity);

        } catch (Exception e) {
            log.error("Error publishing activity: {}, {}", activity.getEventType().name(),
                    activity.getMetadata().getOrDefault("actionBy", ""));
        }
    }
}
