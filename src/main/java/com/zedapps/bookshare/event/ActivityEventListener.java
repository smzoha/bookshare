package com.zedapps.bookshare.event;

import com.zedapps.bookshare.dto.activity.ActivityEvent;
import com.zedapps.bookshare.entity.activity.Activity;
import com.zedapps.bookshare.service.activity.ActivityService;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * @author smzoha
 * @since 13/2/26
 **/
@Component
public class ActivityEventListener {

    private final ActivityService activityService;

    public ActivityEventListener(ActivityService activityService) {
        this.activityService = activityService;
    }

    @Async
    @EventListener
    public void handleActivityPublish(ActivityEvent activityEvent) {
        Activity activity = Activity.builder()
                .login(activityEvent.login())
                .eventType(activityEvent.eventType())
                .referenceEntity(activityEvent.referenceEntity())
                .referenceId(activityEvent.referenceId())
                .metadata(activityEvent.metadata())
                .build();

        activityService.saveActivity(activity);
    }
}
