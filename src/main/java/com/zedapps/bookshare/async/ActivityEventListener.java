package com.zedapps.bookshare.async;

import com.zedapps.bookshare.dto.activity.ActivityEvent;
import com.zedapps.bookshare.entity.activity.Activity;
import com.zedapps.bookshare.service.activity.ActivityService;
import com.zedapps.bookshare.service.login.LoginService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * @author smzoha
 * @since 13/2/26
 **/
@Component
@RequiredArgsConstructor
public class ActivityEventListener {

    private final ActivityService activityService;
    private final LoginService loginService;

    @Async
    @EventListener
    public void handleActivityPublish(ActivityEvent activityEvent) {
        Activity activity = Activity.builder()
                .login(loginService.getLogin(activityEvent.loginEmail()))
                .eventType(activityEvent.eventType())
                .referenceEntity(activityEvent.eventType().getReferenceEntity())
                .referenceId(activityEvent.referenceId())
                .metadata(activityEvent.metadata())
                .internal(activityEvent.internal())
                .build();

        activityService.saveActivity(activity);
    }
}
