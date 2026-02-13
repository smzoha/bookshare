package com.zedapps.bookshare.security;

import com.zedapps.bookshare.dto.login.LoginDetails;
import com.zedapps.bookshare.entity.activity.enums.ActivityType;
import com.zedapps.bookshare.service.activity.ActivityService;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.authentication.event.LogoutSuccessEvent;
import org.springframework.stereotype.Component;

import java.util.Collections;

/**
 * @author smzoha
 * @since 13/2/26
 **/
@Component
public class AuthEventListener {

    private final ActivityService activityService;

    public AuthEventListener(ActivityService activityService) {
        this.activityService = activityService;
    }

    @EventListener
    public void onSuccessfulLogin(AuthenticationSuccessEvent event) {
        LoginDetails loginDetails = (LoginDetails) event.getAuthentication().getPrincipal();

        activityService.saveActivity(ActivityType.LOGIN, loginDetails.getEmail(), null,
                Collections.singletonMap("actionBy", loginDetails.getEmail()));
    }

    @EventListener
    public void onSuccessfulLogout(LogoutSuccessEvent event) {
        LoginDetails loginDetails = (LoginDetails) event.getAuthentication().getPrincipal();

        activityService.saveActivity(ActivityType.LOGOUT, loginDetails.getEmail(), null,
                Collections.singletonMap("actionBy", loginDetails.getEmail()));
    }
}
