package com.zedapps.bookshare.async;

import com.zedapps.bookshare.entity.activity.Activity;
import com.zedapps.bookshare.entity.activity.ActivityOutbox;
import com.zedapps.bookshare.entity.login.Login;
import com.zedapps.bookshare.enums.ActivityStatus;
import com.zedapps.bookshare.service.activity.ActivityService;
import com.zedapps.bookshare.service.login.LoginService;
import com.zedapps.bookshare.util.TestUtils;
import jakarta.persistence.NoResultException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataRetrievalFailureException;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * @author smzoha
 * @since 7/6/26
 **/
@ExtendWith(MockitoExtension.class)
public class ActivityOutboxProcessorTest {

    @InjectMocks
    private ActivityOutboxProcessor activityOutboxProcessor;

    @Mock
    private ActivityService activityService;

    @Mock
    private LoginService loginService;

    private Login login;
    private ActivityOutbox activityOutbox;

    @BeforeEach
    void setUp() {
        login = TestUtils.getLogin("test@test.com", "test", true);
        login.setId(1L);

        activityOutbox = TestUtils.getActivityOutboxItem(ActivityStatus.PENDING);
    }

    @Test
    void processOutbox_noUnprocessedItems_doesNothing() {
        when(activityService.getUnprocessedActivityOutboxItems()).thenReturn(List.of());

        activityOutboxProcessor.processOutbox();

        verify(activityService, never()).processOutboxActivity(anyList(), anyList());
    }

    @Test
    void processOutbox_payloadHasActionBy_buildsActivityWithLoginAndMarksCompleted() {
        activityOutbox.setPayload(Map.of("actionBy", login.getEmail()));

        when(activityService.getUnprocessedActivityOutboxItems()).thenReturn(List.of(activityOutbox));
        when(loginService.getLogin(login.getEmail())).thenReturn(login);

        activityOutboxProcessor.processOutbox();

        assertEquals(ActivityStatus.COMPLETED, activityOutbox.getStatus());
        assertNotNull(activityOutbox.getProcessedAt());

        verify(loginService).getLogin(login.getEmail());

        ArgumentCaptor<List<Activity>> captor = processedActivityCaptor();
        verify(activityService).processOutboxActivity(eq(List.of(activityOutbox)), captor.capture());

        List<Activity> processedActivities = captor.getValue();
        assertEquals(1, processedActivities.size());

        Activity activity = processedActivities.getFirst();

        assertEquals(login, activity.getLogin());
        assertEquals(activityOutbox.getEventType(), activity.getEventType());
        assertEquals(activityOutbox.getReferenceEntity(), activity.getReferenceEntity());
        assertEquals(activityOutbox.getReferenceId(), activity.getReferenceId());
        assertEquals(activityOutbox.getPayload(), activity.getMetadata());
        assertTrue(activity.isInternal());
    }

    @Test
    void processOutbox_payloadMissingActionBy_buildsActivityWithNullLogin() {
        activityOutbox.setPayload(Map.of());

        when(activityService.getUnprocessedActivityOutboxItems()).thenReturn(List.of(activityOutbox));

        activityOutboxProcessor.processOutbox();

        assertEquals(ActivityStatus.COMPLETED, activityOutbox.getStatus());

        verifyNoInteractions(loginService);

        ArgumentCaptor<List<Activity>> captor = processedActivityCaptor();
        verify(activityService).processOutboxActivity(eq(List.of(activityOutbox)), captor.capture());

        assertNull(captor.getValue().getFirst().getLogin());
    }

    @Test
    void processOutbox_loginLookupThrowsNoResultException_marksItemFailed() {
        activityOutbox.setPayload(Map.of("actionBy", login.getEmail()));

        when(activityService.getUnprocessedActivityOutboxItems()).thenReturn(List.of(activityOutbox));
        when(loginService.getLogin(login.getEmail())).thenThrow(new NoResultException());

        activityOutboxProcessor.processOutbox();

        assertEquals(ActivityStatus.FAILED, activityOutbox.getStatus());
        assertNotNull(activityOutbox.getProcessedAt());

        verify(activityService).processOutboxActivity(eq(List.of(activityOutbox)), eq(List.of()));
    }

    @Test
    void processOutbox_loginLookupThrowsIllegalArgumentException_marksItemFailed() {
        activityOutbox.setPayload(Map.of("actionBy", login.getEmail()));

        when(activityService.getUnprocessedActivityOutboxItems()).thenReturn(List.of(activityOutbox));
        when(loginService.getLogin(login.getEmail())).thenThrow(new IllegalArgumentException());

        activityOutboxProcessor.processOutbox();

        assertEquals(ActivityStatus.FAILED, activityOutbox.getStatus());

        verify(activityService).processOutboxActivity(eq(List.of(activityOutbox)), eq(List.of()));
    }

    @Test
    void processOutbox_loginLookupThrowsDataAccessExceptionBelowRetryLimit_marksPendingAndIncrementsRetryCount() {
        activityOutbox.setPayload(Map.of("actionBy", login.getEmail()));
        activityOutbox.setRetryCount(1);

        when(activityService.getUnprocessedActivityOutboxItems()).thenReturn(List.of(activityOutbox));
        when(loginService.getLogin(login.getEmail())).thenThrow(new DataRetrievalFailureException("DB unavailable"));

        activityOutboxProcessor.processOutbox();

        assertEquals(ActivityStatus.PENDING, activityOutbox.getStatus());
        assertEquals(2, activityOutbox.getRetryCount());
        assertNotNull(activityOutbox.getProcessedAt());

        verify(activityService).processOutboxActivity(eq(List.of(activityOutbox)), eq(List.of()));
    }

    @Test
    void processOutbox_loginLookupThrowsDataAccessExceptionAtRetryLimit_marksItemFailed() {
        activityOutbox.setPayload(Map.of("actionBy", login.getEmail()));
        activityOutbox.setRetryCount(3);

        when(activityService.getUnprocessedActivityOutboxItems()).thenReturn(List.of(activityOutbox));
        when(loginService.getLogin(login.getEmail())).thenThrow(new DataRetrievalFailureException("DB unavailable"));

        activityOutboxProcessor.processOutbox();

        assertEquals(ActivityStatus.FAILED, activityOutbox.getStatus());
        assertEquals(3, activityOutbox.getRetryCount());

        verify(activityService).processOutboxActivity(eq(List.of(activityOutbox)), eq(List.of()));
    }

    @Test
    void cleanupStaleOutboxActivity_staleItemsExist_deletesThem() {
        List<ActivityOutbox> staleItems = List.of(TestUtils.getActivityOutboxItem(ActivityStatus.COMPLETED));

        when(activityService.getProcessedActivityOutboxItems()).thenReturn(staleItems);

        activityOutboxProcessor.cleanupStaleOutboxActivity();

        verify(activityService).deleteOutboxActivityList(staleItems);
    }

    @Test
    void cleanupStaleOutboxActivity_deletionFails_doesNotPropagateException() {
        List<ActivityOutbox> staleItems = List.of(TestUtils.getActivityOutboxItem(ActivityStatus.FAILED));

        when(activityService.getProcessedActivityOutboxItems()).thenReturn(staleItems);
        doThrow(new DataRetrievalFailureException("Delete Failed")).when(activityService).deleteOutboxActivityList(staleItems);

        assertDoesNotThrow(() -> activityOutboxProcessor.cleanupStaleOutboxActivity());

        verify(activityService).deleteOutboxActivityList(staleItems);
    }

    @SuppressWarnings("unchecked")
    private ArgumentCaptor<List<Activity>> processedActivityCaptor() {
        return ArgumentCaptor.forClass(List.class);
    }
}
