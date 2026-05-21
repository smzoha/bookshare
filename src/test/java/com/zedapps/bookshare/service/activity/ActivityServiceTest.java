package com.zedapps.bookshare.service.activity;

import com.zedapps.bookshare.dto.activity.ActivityFeedDto;
import com.zedapps.bookshare.entity.activity.Activity;
import com.zedapps.bookshare.entity.activity.ActivityOutbox;
import com.zedapps.bookshare.entity.login.Login;
import com.zedapps.bookshare.enums.ActivityStatus;
import com.zedapps.bookshare.enums.ActivityType;
import com.zedapps.bookshare.repository.activity.ActivityOutboxRepository;
import com.zedapps.bookshare.repository.activity.ActivityRepository;
import com.zedapps.bookshare.util.TestUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * @author smzoha
 * @since 5/5/26
 **/
@ExtendWith(MockitoExtension.class)
public class ActivityServiceTest {

    @InjectMocks
    private ActivityService activityService;

    @Mock
    private ActivityRepository activityRepository;

    @Mock
    private ActivityOutboxRepository activityOutboxRepository;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    private Login login;
    private List<ActivityOutbox> unprocessedActivityOutboxList;
    private List<ActivityOutbox> processedActivityOutboxList;

    @BeforeEach
    public void setup() {
        login = TestUtils.getLogin("test@test.com", "test", true);

        unprocessedActivityOutboxList = new ArrayList<>();
        processedActivityOutboxList = new ArrayList<>();

        for (int i = 0; i < 35; i++) {
            unprocessedActivityOutboxList.add(TestUtils.getActivityOutboxItem(ActivityStatus.PENDING));
            processedActivityOutboxList.add(TestUtils.getActivityOutboxItem(ActivityStatus.FAILED));
            processedActivityOutboxList.add(TestUtils.getActivityOutboxItem(ActivityStatus.COMPLETED));
        }
    }

    @Test
    void getUnprocessedActivityOutboxItems_pendingItemsExist_returnsItems() {
        when(activityOutboxRepository.findTop100ByStatusOrderByCreatedAt(ActivityStatus.PENDING))
                .thenReturn(unprocessedActivityOutboxList);

        List<ActivityOutbox> activityOutboxList = activityService.getUnprocessedActivityOutboxItems();
        assertFalse(activityOutboxList.isEmpty());
        assertTrue(activityOutboxList.stream().allMatch(activityOutbox -> activityOutbox.getStatus() == ActivityStatus.PENDING));
    }

    @Test
    void getProcessedActivityOutboxItems_completedAndFailedItemsExist_returnsItems() {
        when(activityOutboxRepository.findByStatusIn(List.of(ActivityStatus.COMPLETED, ActivityStatus.FAILED)))
                .thenReturn(processedActivityOutboxList);

        List<ActivityOutbox> activityOutboxList = activityService.getProcessedActivityOutboxItems();
        assertFalse(activityOutboxList.isEmpty());
        assertTrue(activityOutboxList.stream().allMatch(activityOutbox ->
                activityOutbox.getStatus() == ActivityStatus.COMPLETED || activityOutbox.getStatus() == ActivityStatus.FAILED));
    }

    @Test
    void saveActivityOutbox_validInput_savesWithPendingStatus() {
        assertDoesNotThrow(() -> activityService.saveActivityOutbox(ActivityType.FRIEND_REQ_SENT,
                1L, Map.of()));

        ArgumentCaptor<ActivityOutbox> captor = ArgumentCaptor.forClass(ActivityOutbox.class);
        verify(activityOutboxRepository).save(captor.capture());

        assertEquals(ActivityStatus.PENDING, captor.getValue().getStatus());
    }

    @Test
    void saveActivityOutbox_repositoryThrowsException_doesNotPropagate() {
        when(activityOutboxRepository.save(any())).thenThrow(DataIntegrityViolationException.class);

        assertDoesNotThrow(() -> activityService.saveActivityOutbox(ActivityType.FRIEND_REQ_SENT,
                1L, Map.of()));

        verify(activityOutboxRepository).save(any(ActivityOutbox.class));
    }

    @Test
    void saveActivity_nonInternalType_publishesActivityFeedDtoEvent() {
        Activity activity = TestUtils.getActivity(ActivityType.ADD_FRIEND);
        activity.setLogin(login);

        when(activityRepository.save(any())).thenReturn(activity);

        activityService.saveActivity(ActivityType.ADD_FRIEND, login, 1L, Map.of());
        verify(applicationEventPublisher).publishEvent(any(ActivityFeedDto.class));
    }

    @Test
    void saveActivity_internalType_doesNotPublishEvent() {
        Activity activity = TestUtils.getActivity(ActivityType.FRIEND_REQ_SENT);
        activity.setLogin(login);

        when(activityRepository.save(any())).thenReturn(activity);

        activityService.saveActivity(ActivityType.FRIEND_REQ_SENT, login, 1L, Map.of());
        verify(applicationEventPublisher, never()).publishEvent(any(ActivityFeedDto.class));
    }

    @Test
    void saveActivity_dataAccessExceptionThrown_doesNotPropagate() {
        Activity activity = TestUtils.getActivity(ActivityType.FRIEND_REQ_SENT);
        when(activityRepository.save(any())).thenThrow(DataIntegrityViolationException.class);

        assertDoesNotThrow(() -> activityService.saveActivity(activity));
        verify(activityRepository).save(any(Activity.class));
    }

    @Test
    void saveActivity_illegalArgumentExceptionThrown_doesNotPropagate() {
        Activity activity = TestUtils.getActivity(ActivityType.FRIEND_REQ_SENT);
        when(activityRepository.save(any())).thenThrow(IllegalArgumentException.class);

        assertDoesNotThrow(() -> activityService.saveActivity(activity));
        verify(activityRepository).save(any(Activity.class));
    }

    @Test
    void saveActivity_setsInternalFlagTrue_whenTypeNotInFeedActivities() {
        Activity activity = TestUtils.getActivity(ActivityType.FRIEND_REQ_SENT);
        activity.setLogin(login);

        when(activityRepository.save(any())).thenReturn(activity);

        activityService.saveActivity(activity);
        verify(applicationEventPublisher, never()).publishEvent(any(ActivityFeedDto.class));
    }

    @Test
    void saveActivity_setsInternalFlagFalse_whenTypeInFeedActivities() {
        Activity activity = TestUtils.getActivity(ActivityType.ADD_FRIEND);
        activity.setLogin(login);

        when(activityRepository.save(any())).thenReturn(activity);

        activityService.saveActivity(activity);
        verify(applicationEventPublisher).publishEvent(any(ActivityFeedDto.class));
    }

    @Test
    void saveActivityList_multipleActivities_savesEach() {
        Activity activity = TestUtils.getActivity(ActivityType.FRIEND_REQ_SENT);
        Activity activity2 = TestUtils.getActivity(ActivityType.ADD_FRIEND);

        activity.setLogin(login);
        activity2.setLogin(login);

        when(activityRepository.save(any())).thenReturn(activity, activity2);

        activityService.saveActivityList(List.of(activity, activity2));
        verify(activityRepository, times(2)).save(any(Activity.class));
    }

    @Test
    void saveActivityList_emptyList_noInteractionWithRepo() {
        activityService.saveActivityList(Collections.emptyList());
        verify(activityRepository, never()).save(any(Activity.class));
    }

    @Test
    void processOutboxActivity_validLists_persistsBoth() {
        Activity activity = TestUtils.getActivity(ActivityType.FRIEND_REQ_SENT);
        activity.setLogin(login);

        when(activityOutboxRepository.saveAll(any())).thenReturn(processedActivityOutboxList);
        when(activityRepository.save(any())).thenReturn(activity);

        activityService.processOutboxActivity(unprocessedActivityOutboxList, List.of(activity));
        verify(activityOutboxRepository).saveAll(anyList());
        verify(activityRepository).save(any(Activity.class));
    }

    @Test
    void deleteOutboxActivityList_validList_deletesAll() {
        activityService.deleteOutboxActivityList(unprocessedActivityOutboxList);
        verify(activityOutboxRepository).deleteAll(unprocessedActivityOutboxList);
    }
}
