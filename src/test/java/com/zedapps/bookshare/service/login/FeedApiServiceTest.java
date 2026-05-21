package com.zedapps.bookshare.service.login;

import com.zedapps.bookshare.dto.api.feed.FeedApiResponse;
import com.zedapps.bookshare.dto.feed.FeedDto;
import com.zedapps.bookshare.entity.activity.Activity;
import com.zedapps.bookshare.entity.feed.FeedEntry;
import com.zedapps.bookshare.entity.login.Login;
import com.zedapps.bookshare.enums.ActivityType;
import com.zedapps.bookshare.util.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

/**
 * @author smzoha
 * @since 21/5/26
 **/
@ExtendWith(MockitoExtension.class)
public class FeedApiServiceTest {

    @InjectMocks
    private FeedApiService feedApiService;

    @Mock
    private FeedService feedService;

    private Login login;

    @BeforeEach
    void setup() {
        login = TestUtils.getLogin("test@test.com", "test", true);
        login.setId(1L);

        List<FeedEntry> feedEntries = new ArrayList<>();

        setupFeedEntriesStub(feedEntries);
        setupFeedEntryDtoListStub(feedEntries);
    }

    @Test
    void getFeedApiResponse_validRequest_returnsPaginatedResponse() {
        FeedApiResponse feedApiResponse = feedApiService.getFeedApiResponse(login, 10, 0);

        assertNotNull(feedApiResponse);
        assertFalse(feedApiResponse.feedEntries().isEmpty());
    }

    @Test
    void getFeedApiResponse_validRequest_paginationMetadataMatchesPageResult() {
        FeedApiResponse feedApiResponse = feedApiService.getFeedApiResponse(login, 10, 0);

        assertNotNull(feedApiResponse);
        assertEquals(0, feedApiResponse.page());
        assertEquals(2, feedApiResponse.totalPages());
    }

    @Test
    void getFeedApiResponse_emptyFeed_returnsEmptyDtoList() {
        when(feedService.getFeedEntries(eq(login), anyInt(), anyInt()))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        when(feedService.mapToFeedDtoList(any())).thenReturn(Collections.emptyList());

        FeedApiResponse feedApiResponse = feedApiService.getFeedApiResponse(login, 10, 0);

        assertNotNull(feedApiResponse);
        assertTrue(feedApiResponse.feedEntries().isEmpty());
    }

    private void setupFeedEntriesStub(List<FeedEntry> feedEntries) {
        for (int i = 0; i < 20; i++) {
            Activity activity = getAddFriendActivity(i, i);
            FeedEntry feedEntry = new FeedEntry((long) i, login, activity, LocalDateTime.now());

            feedEntries.add(feedEntry);
        }

        Pageable pageable = PageRequest.of(0, 10);

        lenient().when(feedService.getFeedEntries(eq(login), anyInt(), anyInt()))
                .thenReturn(new PageImpl<>(feedEntries, pageable, feedEntries.size()));
    }

    private void setupFeedEntryDtoListStub(List<FeedEntry> feedEntries) {
        List<FeedDto> feedDtoList = feedEntries.stream()
                .map(entry -> new FeedDto(entry.getActivity().getEventType().name(),
                        entry.getAudienceLogin(),
                        "Add Friend Message",
                        null,
                        Collections.emptyMap(),
                        "1d"))
                .toList();

        when(feedService.mapToFeedDtoList(any())).thenReturn(feedDtoList);
    }

    private Activity getAddFriendActivity(long id, int days) {
        Activity activity = TestUtils.getActivity(ActivityType.ADD_FRIEND);
        activity.setId(id);
        activity.setLogin(login);
        activity.setMetadata(Map.of("requestFromEmail", "friend@test.com"));
        activity.setCreatedAt(LocalDateTime.now().minusDays(days));

        return activity;
    }
}
