package com.zedapps.bookshare.async;

import com.zedapps.bookshare.dto.activity.ActivityFeedDto;
import com.zedapps.bookshare.entity.activity.Activity;
import com.zedapps.bookshare.entity.feed.FeedEntry;
import com.zedapps.bookshare.entity.login.Connection;
import com.zedapps.bookshare.entity.login.Login;
import com.zedapps.bookshare.enums.ActivityType;
import com.zedapps.bookshare.repository.feed.FeedEntryRepository;
import com.zedapps.bookshare.repository.login.ConnectionRepository;
import com.zedapps.bookshare.util.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author smzoha
 * @since 6/6/26
 **/
@ExtendWith(MockitoExtension.class)
public class FeedEntryListenerTest {

    @InjectMocks
    private FeedEntryListener feedEntryListener;

    @Mock
    private ConnectionRepository connectionRepository;

    @Mock
    private FeedEntryRepository feedEntryRepository;

    private Login login;
    private ActivityFeedDto activityFeedDto;
    private List<Connection> connections;

    @BeforeEach
    void setUp() {
        login = TestUtils.getLogin("test@test.com", "test", true);
        login.setId(1L);

        Activity activity = TestUtils.getActivity(ActivityType.BOOK_ADD_REVIEW);
        activityFeedDto = ActivityFeedDto.builder()
                .activity(activity)
                .login(login)
                .build();

        connections = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            Login l = TestUtils.getLogin(String.format("test%d@test.com", (i + 2)),
                    String.format("test%d", (i + 2)), true);

            Connection connection = new Connection();
            connection.setId((long) i + 1);
            connection.setPerson1(login);
            connection.setPerson2(l);

            connections.add(connection);
        }
    }

    @Test
    void onActivityPublish_withConnections_persistsFeedEntryPerPerson2() {
        when(connectionRepository.findConnectionsByPerson1(login)).thenReturn(connections);

        feedEntryListener.onActivityPublish(activityFeedDto);

        verify(connectionRepository).findConnectionsByPerson1(login);
        verify(feedEntryRepository).saveAll(ArgumentMatchers.<List<FeedEntry>>argThat(entries ->
                entries.size() == connections.size()
                        && IntStream.range(0, entries.size())
                        .allMatch(i -> entries.get(i).getAudienceLogin().equals(connections.get(i).getPerson2())
                                && entries.get(i).getActivity().equals(activityFeedDto.activity()))));
    }

    @Test
    void onActivityPublish_noConnections_saveAllWithEmptyList() {
        when(connectionRepository.findConnectionsByPerson1(login)).thenReturn(List.of());

        feedEntryListener.onActivityPublish(activityFeedDto);

        verify(connectionRepository).findConnectionsByPerson1(login);
        verify(feedEntryRepository).saveAll(List.of());
    }
}
