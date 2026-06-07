package com.zedapps.bookshare.async;

import com.zedapps.bookshare.entity.login.Login;
import com.zedapps.bookshare.enums.ActivityType;
import com.zedapps.bookshare.service.activity.ActivityService;
import com.zedapps.bookshare.service.auth.LoginDetails;
import com.zedapps.bookshare.service.login.LoginService;
import com.zedapps.bookshare.util.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.authentication.event.LogoutSuccessEvent;
import org.springframework.security.core.Authentication;

import java.util.Collections;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author smzoha
 * @since 7/6/26
 **/
@ExtendWith(MockitoExtension.class)
public class AuthEventListenerTest {

    @InjectMocks
    private AuthEventListener authEventListener;

    @Mock
    private ActivityService activityService;

    @Mock
    private LoginService loginService;

    @Mock
    private Authentication authentication;

    private Login login;
    private LoginDetails loginDetails;

    @BeforeEach
    void setUp() {
        login = TestUtils.getLogin("test@test.com", "test", true);
        login.setId(1L);

        loginDetails = TestUtils.getLoginDetails(login.getEmail(), login.getHandle(), login.isActive());

        when(authentication.getPrincipal()).thenReturn(loginDetails);
    }

    @Test
    void onSuccessfulLogin_validEvent_persistLoginActivity() {
        AuthenticationSuccessEvent event = new AuthenticationSuccessEvent(authentication);

        when(loginService.getLogin(loginDetails.getEmail())).thenReturn(login);

        authEventListener.onSuccessfulLogin(event);

        verify(loginService).getLogin(loginDetails.getEmail());
        verify(activityService).saveActivity(ActivityType.LOGIN, login, null,
                Collections.singletonMap("actionBy", loginDetails.getEmail()));
    }

    @Test
    void onSuccessfulLogout_validEvent_persistLogoutActivity() {
        LogoutSuccessEvent event = new LogoutSuccessEvent(authentication);

        when(loginService.getLogin(loginDetails.getEmail())).thenReturn(login);

        authEventListener.onSuccessfulLogout(event);

        verify(loginService).getLogin(loginDetails.getEmail());
        verify(activityService).saveActivity(ActivityType.LOGOUT, login, null,
                Collections.singletonMap("actionBy", loginDetails.getEmail()));
    }
}
