package com.zedapps.bookshare.service;

import com.zedapps.bookshare.entity.activity.ActivityOutbox;
import com.zedapps.bookshare.entity.activity.enums.ActivityStatus;
import com.zedapps.bookshare.entity.activity.enums.ActivityType;
import com.zedapps.bookshare.repository.activity.ActivityOutboxRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * @author smzoha
 * @since 13/2/26
 **/
@Service
public class ActivityService {

    private final ActivityOutboxRepository activityOutboxRepository;

    public ActivityService(ActivityOutboxRepository activityOutboxRepository) {
        this.activityOutboxRepository = activityOutboxRepository;
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

        activityOutboxRepository.save(activityOutbox);
    }
}
