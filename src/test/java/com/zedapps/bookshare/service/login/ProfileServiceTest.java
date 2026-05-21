package com.zedapps.bookshare.service.login;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.zedapps.bookshare.entity.book.Author;
import com.zedapps.bookshare.entity.book.Book;
import com.zedapps.bookshare.entity.login.Connection;
import com.zedapps.bookshare.entity.login.FriendRequest;
import com.zedapps.bookshare.entity.login.Login;
import com.zedapps.bookshare.entity.login.ReadingProgress;
import com.zedapps.bookshare.enums.ActivityType;
import com.zedapps.bookshare.enums.ConnectionAction;
import com.zedapps.bookshare.enums.Status;
import com.zedapps.bookshare.repository.login.ConnectionRepository;
import com.zedapps.bookshare.repository.login.FriendRequestRepository;
import com.zedapps.bookshare.service.activity.ActivityService;
import com.zedapps.bookshare.util.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * @author smzoha
 * @since 18/5/26
 **/
@ExtendWith(MockitoExtension.class)
public class ProfileServiceTest {

    @InjectMocks
    private ProfileService profileService;

    @Mock
    private FriendRequestRepository friendRequestRepository;

    @Mock
    private ConnectionRepository connectionRepository;

    @Mock
    private ActivityService activityService;

    private Login login1;
    private Login login2;

    private FriendRequest friendRequest;
    private Connection connection;
    private Connection reverseConnection;

    private List<Book> books;
    private Set<ReadingProgress> readingProgresses;
    private ReadingProgress completedProgress;

    private ListAppender<ILoggingEvent> logAppender;

    @BeforeEach
    void setup() {
        login1 = TestUtils.getLogin("login1@test.com", "login1", true);
        login1.setId(1L);

        login2 = TestUtils.getLogin("login2@test.com", "login2", true);
        login2.setId(2L);

        setupFriendRequestAndConnections();
        setupReadingProgresses();

        FriendRequest unsavedFriendRequest = new FriendRequest();
        unsavedFriendRequest.setPerson1(login1);
        unsavedFriendRequest.setPerson2(login2);

        lenient().when(friendRequestRepository.findFriendRequest(login1, login2))
                .thenReturn(Optional.of(friendRequest));

        lenient().when(friendRequestRepository.findFriendRequest(login2, login1))
                .thenReturn(Optional.of(friendRequest));

        lenient().when(friendRequestRepository.save(any())).thenReturn(friendRequest);

        lenient().when(connectionRepository.save(any()))
                .thenReturn(connection)
                .thenReturn(reverseConnection);

        Logger logger = (Logger) LoggerFactory.getLogger(ProfileService.class);
        logAppender = new ListAppender<>();
        logAppender.start();
        logger.addAppender(logAppender);
    }

    @AfterEach
    void teardown() {
        Logger logger = (Logger) LoggerFactory.getLogger(ProfileService.class);
        logger.detachAppender(logAppender);
    }

    @Test
    void performConnectionAction_sendFriendRequest_createsPendingRequest() {
        profileService.performConnectionAction(login1, login2, ConnectionAction.SEND_FRIEND_REQ);

        verify(friendRequestRepository).save(any());
    }

    @Test
    void performConnectionAction_sendFriendRequest_firesOutbox() {
        profileService.performConnectionAction(login1, login2, ConnectionAction.SEND_FRIEND_REQ);

        verify(activityService).saveActivityOutbox(eq(ActivityType.FRIEND_REQ_SENT),
                eq(friendRequest.getId()), anyMap());
    }

    @Test
    void performConnectionAction_revoke_deletesExistingRequest() {
        profileService.performConnectionAction(login1, login2, ConnectionAction.REVOKE_FRIEND_REQ);

        verify(friendRequestRepository).findFriendRequest(eq(login1), eq(login2));
        verify(friendRequestRepository).delete(eq(friendRequest));
    }

    @Test
    void performConnectionAction_revoke_logsErrorIfRequestMissing() {
        when(friendRequestRepository.findFriendRequest(login1, login2)).thenReturn(Optional.empty());

        profileService.performConnectionAction(login1, login2, ConnectionAction.REVOKE_FRIEND_REQ);

        assertTrue(logAppender.list.stream()
                .anyMatch(logEvent -> logEvent.getLevel() == Level.ERROR
                        && logEvent.getFormattedMessage().startsWith("Friend request does not exist.")));

        verify(friendRequestRepository, never()).delete(any());
    }

    @Test
    void performConnectionAction_revoke_firesOutbox() {
        profileService.performConnectionAction(login1, login2, ConnectionAction.REVOKE_FRIEND_REQ);

        verify(activityService).saveActivityOutbox(eq(ActivityType.REVOKE_FRIEND_REQ),
                eq(friendRequest.getId()), anyMap());
    }

    @Test
    void performConnectionAction_accept_logsErrorIfRequestMissing() {
        when(friendRequestRepository.findFriendRequest(login2, login1)).thenReturn(Optional.empty());

        profileService.performConnectionAction(login1, login2, ConnectionAction.ACCEPT_FRIEND_REQ);

        assertTrue(logAppender.list.stream()
                .anyMatch(logEvent -> logEvent.getLevel() == Level.ERROR
                        && logEvent.getFormattedMessage().startsWith("Friend request does not exist.")));

        verify(connectionRepository, never()).save(any());
    }

    @Test
    void performConnectionAction_accept_createsBidirectionalConnections() {
        profileService.performConnectionAction(login1, login2, ConnectionAction.ACCEPT_FRIEND_REQ);

        verify(friendRequestRepository).delete(eq(friendRequest));
        verify(connectionRepository, times(2)).save(any());
    }

    @Test
    void performConnectionAction_accept_firesAddFriendOutbox() {
        profileService.performConnectionAction(login1, login2, ConnectionAction.ACCEPT_FRIEND_REQ);

        verify(activityService).saveActivityOutbox(eq(ActivityType.ADD_FRIEND),
                eq(connection.getId()), anyMap());
    }

    @Test
    void performConnectionAction_decline_logsErrorIfRequestMissing() {
        when(friendRequestRepository.findFriendRequest(login2, login1)).thenReturn(Optional.empty());

        profileService.performConnectionAction(login1, login2, ConnectionAction.DECLINE_FRIEND_REQ);

        assertTrue(logAppender.list.stream()
                .anyMatch(logEvent -> logEvent.getLevel() == Level.ERROR
                        && logEvent.getFormattedMessage().startsWith("Friend request does not exist.")));

        verify(friendRequestRepository, never()).delete(any());
    }

    @Test
    void performConnectionAction_decline_deletesRequestAndFiresOutbox() {
        profileService.performConnectionAction(login1, login2, ConnectionAction.DECLINE_FRIEND_REQ);

        verify(friendRequestRepository).delete(eq(friendRequest));
        verify(activityService).saveActivityOutbox(eq(ActivityType.DECLINE_FRIEND_REQ),
                eq(friendRequest.getId()), anyMap());
    }

    @Test
    void performConnectionAction_removeFriend_deletesBothDirectionalConnections() {
        when(connectionRepository.findConnectionByPerson1AndPerson2(login1, login2)).thenReturn(connection);
        when(connectionRepository.findConnectionByPerson1AndPerson2(login2, login1)).thenReturn(reverseConnection);

        profileService.performConnectionAction(login1, login2, ConnectionAction.REMOVE_FRIEND);

        verify(connectionRepository).delete(eq(connection));
        verify(connectionRepository).delete(eq(reverseConnection));

        verify(activityService).saveActivityOutbox(eq(ActivityType.REMOVE_FRIEND),
                eq(connection.getId()), anyMap());
    }

    @Test
    void getDistinctReadingProgressList_duplicateBooksInProgress_keepsFirstOccurrencePerBook() {
        List<ReadingProgress> distinctReadingProgressList = profileService.getDistinctReadingProgressList(login1);
        Book book = distinctReadingProgressList.getFirst().getBook();

        List<ReadingProgress> bookReadingProgress = readingProgresses.stream()
                .filter(rp -> Objects.equals(book, rp.getBook()))
                .toList();

        assertFalse(distinctReadingProgressList.contains(bookReadingProgress.getLast()));
        assertTrue(distinctReadingProgressList.contains(bookReadingProgress.getFirst()));
    }

    @Test
    void getDistinctReadingProgressList_moreThanFiveBooks_limitsToFive() {
        List<ReadingProgress> distinctReadingProgressList = profileService.getDistinctReadingProgressList(login1);

        assertTrue(readingProgresses.size() > 5);
        assertEquals(5, distinctReadingProgressList.size());
    }

    @Test
    void getDistinctReadingProgressList_completedProgressEntries_excludesCompleted() {
        List<ReadingProgress> distinctReadingProgressList = profileService.getDistinctReadingProgressList(login1);

        assertFalse(distinctReadingProgressList.contains(completedProgress));
    }

    @Test
    void getDistinctReadingProgressList_multipleEntries_sortsByStartDateDesc() {
        List<ReadingProgress> distinctReadingProgressList = profileService.getDistinctReadingProgressList(login1);

        assertTrue(distinctReadingProgressList.getFirst().getStartDate()
                .isAfter(distinctReadingProgressList.getLast().getStartDate()));
    }

    private void setupFriendRequestAndConnections() {
        friendRequest = new FriendRequest();
        friendRequest.setId(1L);
        friendRequest.setPerson1(login1);
        friendRequest.setPerson2(login2);

        connection = new Connection();
        connection.setId(1L);
        connection.setPerson1(login1);
        connection.setPerson2(login2);

        reverseConnection = new Connection();
        reverseConnection.setId(2L);
        reverseConnection.setPerson1(login2);
        reverseConnection.setPerson2(login1);
    }

    private void setupReadingProgresses() {
        books = new ArrayList<>();
        long bookId = 0L;

        Author author = TestUtils.getAuthor("Test", "Author");

        for (String isbn : TestUtils.TEST_ISBN_DATA) {
            Book book = TestUtils.getBook("Book " + isbn, isbn, author, Status.ACTIVE);
            book.setId(++bookId);
            books.add(book);
        }

        readingProgresses = new LinkedHashSet<>();

        for (int i = 1, j = 0; i <= 20; i++) {
            LocalDate startDate = LocalDateTime.now().minusDays(i).toLocalDate();

            ReadingProgress readingProgress = new ReadingProgress((long) i, login1, books.get(j),
                    10L, startDate, null, false, LocalDateTime.now());

            readingProgresses.add(readingProgress);

            if (i % 3 == 0) j++; // Trying to add multiple reading progresses per book (i.e. 3)
        }

        completedProgress = new ReadingProgress(100L, login1, books.getLast(),
                10L, LocalDateTime.now().minusDays(20).toLocalDate(), null,
                true, LocalDateTime.now());

        readingProgresses.add(completedProgress);

        login1.setReadingProgresses(readingProgresses);
    }
}
