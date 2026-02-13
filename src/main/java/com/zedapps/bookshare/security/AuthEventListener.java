package com.zedapps.bookshare.security;

import com.zedapps.bookshare.dto.login.LoginDetails;
import com.zedapps.bookshare.entity.activity.enums.ActivityType;
import com.zedapps.bookshare.entity.login.Login;
import com.zedapps.bookshare.service.activity.ActivityService;
import com.zedapps.bookshare.service.login.LoginService;
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
    private final LoginService loginService;

    public AuthEventListener(ActivityService activityService, LoginService loginService) {
        this.activityService = activityService;
        this.loginService = loginService;
    }

    @EventListener
    public void onSuccessfulLogin(AuthenticationSuccessEvent event) {
        LoginDetails loginDetails = (LoginDetails) event.getAuthentication().getPrincipal();
        Login login = loginService.getLogin(loginDetails.getEmail());

        activityService.saveActivity(ActivityType.LOGIN, login, null,
                Collections.singletonMap("actionBy", loginDetails.getEmail()));
    }

    @EventListener
    public void onSuccessfulLogout(LogoutSuccessEvent event) {
        LoginDetails loginDetails = (LoginDetails) event.getAuthentication().getPrincipal();
        Login login = loginService.getLogin(loginDetails.getEmail());

        activityService.saveActivity(ActivityType.LOGOUT, login, null,
                Collections.singletonMap("actionBy", loginDetails.getEmail()));
    }
}
