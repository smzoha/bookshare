package com.zedapps.bookshare.async;

import com.zedapps.bookshare.dto.activity.ActivityEvent;
import com.zedapps.bookshare.entity.login.Login;
import com.zedapps.bookshare.enums.ActivityType;
import com.zedapps.bookshare.service.activity.ActivityService;
import com.zedapps.bookshare.service.login.LoginService;
import com.zedapps.bookshare.util.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Objects;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author smzoha
 * @since 7/6/26
 **/
@ExtendWith(MockitoExtension.class)
public class ActivityEventListenerTest {

    @InjectMocks
    private ActivityEventListener activityEventListener;

    @Mock
    private ActivityService activityService;

    @Mock
    private LoginService loginService;

    private ActivityEvent activityEvent;
    private Login login;

    @BeforeEach
    void setUp() {
        login = TestUtils.getLogin("test@test.com", "test", true);
        login.setId(1L);

        activityEvent = ActivityEvent.builder()
                .eventType(ActivityType.LOGIN)
                .loginEmail(login.getEmail())
                .referenceId(login.getId())
                .metadata(Map.of("actionBy", login.getEmail()))
                .internal(true)
                .build();
    }

    @Test
    void handleActivityPublish_withActivityEvent_persistActivity() {
        when(loginService.getLogin(login.getEmail())).thenReturn(login);

        activityEventListener.handleActivityPublish(activityEvent);

        verify(loginService).getLogin(login.getEmail());

        verify(activityService).saveActivity(argThat(
                activity -> Objects.equals(activity.getLogin(), login)
                        && Objects.equals(activity.getEventType(), activityEvent.eventType())
                        && Objects.equals(activity.getReferenceEntity(), activityEvent.eventType().getReferenceEntity())
                        && Objects.equals(activity.getReferenceId(), activityEvent.referenceId())
                        && Objects.equals(activity.getMetadata(), activityEvent.metadata())
                        && activity.isInternal() == activityEvent.internal()));
    }

    @Test
    void handleActivityPublish_loginNotFound_persistActivityWithNullLogin() {
        when(loginService.getLogin(login.getEmail())).thenReturn(null);

        activityEventListener.handleActivityPublish(activityEvent);

        verify(loginService).getLogin(login.getEmail());
        verify(activityService).saveActivity(argThat(activity -> Objects.isNull(activity.getLogin())
                && Objects.equals(activity.getEventType(), activityEvent.eventType())));
    }
}
