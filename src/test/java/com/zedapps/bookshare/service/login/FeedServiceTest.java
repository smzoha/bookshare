package com.zedapps.bookshare.service.login;

import com.zedapps.bookshare.dto.feed.FeedDto;
import com.zedapps.bookshare.entity.activity.Activity;
import com.zedapps.bookshare.entity.book.Author;
import com.zedapps.bookshare.entity.book.Book;
import com.zedapps.bookshare.entity.feed.FeedEntry;
import com.zedapps.bookshare.entity.login.Login;
import com.zedapps.bookshare.entity.login.Review;
import com.zedapps.bookshare.enums.ActivityType;
import com.zedapps.bookshare.enums.Status;
import com.zedapps.bookshare.repository.feed.FeedEntryRepository;
import com.zedapps.bookshare.repository.login.ReviewRepository;
import com.zedapps.bookshare.service.book.BookService;
import com.zedapps.bookshare.util.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

/**
 * @author smzoha
 * @since 20/5/26
 **/
@ExtendWith(MockitoExtension.class)
public class FeedServiceTest {

    @InjectMocks
    private FeedService feedService;

    @Mock
    private FeedEntryRepository feedEntryRepository;

    @Mock
    private LoginService loginService;

    @Mock
    private BookService bookService;

    @Mock
    private MessageSource messageSource;

    @Mock
    private ReviewRepository reviewRepository;

    private MessageSourceAccessor msa;

    private Login login;
    private Book book;

    private List<FeedEntry> feedEntries;

    @BeforeEach
    void setup() {
        msa = new MessageSourceAccessor(messageSource);
        lenient().when(messageSource.getMessage(anyString(), any(), any(Locale.class))).thenReturn("Test Message");
        ReflectionTestUtils.setField(feedService, "msa", msa);

        login = TestUtils.getLogin("test@test.com", "test", true);
        login.setId(1L);

        Author author = TestUtils.getAuthor("Test", "Author");
        author.setId(1L);

        book = TestUtils.getBook("Book 1", TestUtils.TEST_ISBN_DATA.getFirst(), author, Status.ACTIVE);
        book.setId(1L);

        setupFeedEntries();

        Pageable pageable = PageRequest.of(0, 10);

        lenient().when(bookService.getBook(book.getId())).thenReturn(book);
        lenient().when(loginService.getLogin(login.getEmail())).thenReturn(login);

        lenient().when(feedEntryRepository.getPagedFeedEntries(eq(login),
                        any(LocalDateTime.class), eq(pageable)))
                .thenReturn(new PageImpl<>(feedEntries, pageable, feedEntries.size()));
    }

    @Test
    void getFeedEntries_validRequest_queriesWithThirtyDayCutoff() {
        Page<FeedEntry> pagedFeedEntries = feedService.getFeedEntries(login, 10, 0);
        LocalDateTime now = LocalDateTime.now();

        assertTrue(pagedFeedEntries.hasContent());
        assertTrue(pagedFeedEntries.getContent().stream()
                .allMatch(entry -> Duration.between(entry.getCreatedAt(), now).toDays() <= 30));
    }

    @Test
    void mapToFeedDtoList_someEntriesMapToNull_filtersNullsFromResult() {
        Page<FeedEntry> pagedFeedEntries = feedService.getFeedEntries(login, 10, 0);
        List<FeedDto> feedDtos = feedService.mapToFeedDtoList(pagedFeedEntries);

        assertFalse(feedDtos.isEmpty());
        assertTrue(feedDtos.stream().allMatch(Objects::nonNull));
    }

    @Test
    void getFeedDto_bookAddReviewType_returnsBookDtoWithTruncatedReview() {
        Review review = TestUtils.getReview(book, login, 5);
        review.setId(1L);
        book.setReviews(Set.of(review));

        Activity activity = getBookReviewActivity(ActivityType.BOOK_ADD_REVIEW, review);
        FeedEntry feedEntry = new FeedEntry(1L, login, activity, LocalDateTime.now().minusDays(1));

        when(reviewRepository.findById(review.getId())).thenReturn(Optional.of(review));

        List<FeedDto> feedDtos = feedService.mapToFeedDtoList(new PageImpl<>(List.of(feedEntry),
                PageRequest.of(0, 10), 1));

        FeedDto reviewFeedDto = feedDtos.getFirst();
        String truncDetails = reviewFeedDto.truncDetails();

        assertFalse(truncDetails.isEmpty());
    }

    @Test
    void getFeedDto_reviewContentExceedsFiftyChars_truncatesAtFiftyChars() {
        Review review = TestUtils.getReview(book, login, 5);
        review.setContent("A".repeat(100));
        review.setId(1L);
        book.setReviews(Set.of(review));

        Activity activity = getBookReviewActivity(ActivityType.BOOK_ADD_REVIEW, review);
        FeedEntry feedEntry = new FeedEntry(1L, login, activity, LocalDateTime.now().minusDays(1));

        when(reviewRepository.findById(review.getId())).thenReturn(Optional.of(review));

        List<FeedDto> feedDtos = feedService.mapToFeedDtoList(new PageImpl<>(List.of(feedEntry),
                PageRequest.of(0, 10), 1));

        FeedDto reviewFeedDto = feedDtos.getFirst();
        String truncDetails = reviewFeedDto.truncDetails();

        assertEquals(50, truncDetails.split(" ")[0].length());
    }

    @Test
    void getFeedDto_bookLikeReviewType_includesReviewerNameInMessage() {
        Review review = TestUtils.getReview(book, login, 5);
        review.setId(1L);
        book.setReviews(Set.of(review));

        Activity activity = getBookReviewActivity(ActivityType.BOOK_LIKE_REVIEW, review);
        FeedEntry feedEntry = new FeedEntry(1L, login, activity, LocalDateTime.now().minusDays(1));

        when(reviewRepository.findById(review.getId())).thenReturn(Optional.of(review));
        when(msa.getMessage(eq("feed.activity.book.like.review"),
                any(String[].class), any(Locale.class)))
                .thenReturn(String.format("Liked By %s", login.getName()));

        List<FeedDto> feedDtos = feedService.mapToFeedDtoList(new PageImpl<>(List.of(feedEntry),
                PageRequest.of(0, 10), 1));

        FeedDto reviewFeedDto = feedDtos.getFirst();

        assertTrue(reviewFeedDto.message().contains(login.getName()));
    }

    @Test
    void getFeedDto_bookUpdateReadingProgressType_includesPagesAndTotalInMessage() {
        long pagesRead = 50L;
        long totalPages = book.getPages();

        Activity activity = getReadingProgressUpdateActivity(pagesRead, totalPages);
        FeedEntry feedEntry = new FeedEntry(2L, login, activity, LocalDateTime.now().minusDays(1));

        when(msa.getMessage(eq("feed.activity.update.reading.progress"),
                any(String[].class), any(Locale.class)))
                .thenReturn(String.format("Read %d of %d pages", pagesRead, totalPages));

        List<FeedDto> feedDtos = feedService.mapToFeedDtoList(new PageImpl<>(List.of(feedEntry),
                PageRequest.of(0, 10), 1));

        FeedDto progressFeedDto = feedDtos.getFirst();

        assertTrue(progressFeedDto.message().contains(String.valueOf(pagesRead)));
        assertTrue(progressFeedDto.message().contains(String.valueOf(totalPages)));
    }

    @Test
    void getFeedDto_addFriendType_includesFriendHandleInResponse() {
        Login friend = TestUtils.getLogin("friend@test.com", "friend", true);
        friend.setId(2L);

        Activity activity = getAddFriendActivity(2L, friend.getEmail(), 1);
        FeedEntry feedEntry = new FeedEntry(2L, login, activity, LocalDateTime.now().minusDays(1));

        when(loginService.getLogin(friend.getEmail())).thenReturn(friend);

        List<FeedDto> feedDtos = feedService.mapToFeedDtoList(new PageImpl<>(List.of(feedEntry),
                PageRequest.of(0, 10), 1));

        FeedDto addFriendDto = feedDtos.getFirst();

        assertEquals(friend.getHandle(), addFriendDto.target().get("id"));
    }

    @Test
    void getFeedDto_unknownActivityType_returnsNull() {
        Activity activity = TestUtils.getActivity(ActivityType.LOGIN);
        activity.setId(3L);
        activity.setLogin(login);
        activity.setCreatedAt(LocalDateTime.now().minusDays(1));

        FeedEntry feedEntry = new FeedEntry(3L, login, activity, LocalDateTime.now().minusDays(1));

        List<FeedDto> feedDtos = feedService.mapToFeedDtoList(new PageImpl<>(List.of(feedEntry),
                PageRequest.of(0, 10), 1));

        assertTrue(feedDtos.isEmpty());
    }

    private void setupFeedEntries() {
        feedEntries = new ArrayList<>();

        for (int i = 0; i < 30; i++) {
            Activity activity = getAddFriendActivity(i, login.getEmail(), i);
            FeedEntry feedEntry = new FeedEntry((long) i + 1, login, activity, LocalDateTime.now().minusDays(i));

            feedEntries.add(feedEntry);
        }
    }

    private Activity getBookReviewActivity(ActivityType activityType, Review review) {
        Activity activity = TestUtils.getActivity(activityType);
        activity.setId(1L);
        activity.setLogin(login);

        activity.setMetadata(Map.of(
                "bookId", book.getId(),
                "reviewId", review.getId(),
                "reviewedBy", login.getEmail()
        ));

        activity.setCreatedAt(LocalDateTime.now().minusDays(1));

        return activity;
    }

    private Activity getReadingProgressUpdateActivity(long pagesRead, long totalPages) {
        Activity activity = TestUtils.getActivity(ActivityType.BOOK_UPDATE_READING_PROGRESS);
        activity.setId(2L);
        activity.setLogin(login);
        activity.setMetadata(Map.of(
                "bookId", book.getId(),
                "pagesRead", pagesRead,
                "totalPages", totalPages
        ));

        activity.setCreatedAt(LocalDateTime.now().minusDays(1));

        return activity;
    }

    private Activity getAddFriendActivity(long id, String requestFromEmail, int days) {
        Activity activity = TestUtils.getActivity(ActivityType.ADD_FRIEND);
        activity.setId(id);
        activity.setLogin(login);
        activity.setMetadata(Map.of("requestFromEmail", requestFromEmail));
        activity.setCreatedAt(LocalDateTime.now().minusDays(days));

        return activity;
    }
}
